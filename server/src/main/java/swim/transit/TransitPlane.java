// Copyright 2015-2022 Swim.inc
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package swim.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import swim.actor.ActorHostDef;
import swim.actor.ActorMeshDef;
import swim.actor.ActorPartDef;
import swim.actor.ActorSpace;
import swim.actor.ActorSpaceDef;
import swim.api.agent.AgentDef;
import swim.api.plane.AbstractPlane;
import swim.api.ref.WarpRef;
import swim.api.space.SpaceDef;
import swim.collections.HashTrieMap;
import swim.db.StoreSettings;
import swim.kernel.Kernel;
import swim.kernel.KernelException;
import swim.kernel.KernelLoader;
import swim.system.HostDef;
import swim.system.NodeDef;
import swim.system.PartDef;
import swim.system.PartPredicate;
import swim.server.ServerLoader;
import swim.store.StoreDef;
import swim.store.db.DbStoreDef;
import swim.structure.Item;
import swim.structure.Text;
import swim.structure.Value;
import swim.transit.model.Agency;
import swim.uri.Uri;
import swim.uri.UriMapper;

public class TransitPlane extends AbstractPlane {

  public static void main(String[] args) {
    final Kernel kernel = loadServer(KernelLoader.class.getClassLoader());
    final ActorSpace space = (ActorSpace) kernel.getSpace("transit");
    kernel.start();
    System.out.println("Running Transit App ....");
    kernel.run();

    startAgencies(space);
  }

  public static Kernel loadServer(ClassLoader classLoader) {
    try {
      Value kernelConfig = KernelLoader.loadConfig(classLoader);
      if (kernelConfig == null) {
        kernelConfig = KernelLoader.loadConfigResource(classLoader, "server.recon");
      }
      if (kernelConfig == null) {
        kernelConfig = Value.absent();
      }
      final Kernel kernel = ServerLoader.loadServerStack(classLoader, kernelConfig);
      loadSpaces(kernel, kernelConfig);
      ServerLoader.loadServices(kernel, kernelConfig, classLoader);
      return kernel;
    } catch (IOException cause) {
      throw new KernelException(cause);
    }
  }

  private static void loadSpaces(Kernel kernel, Value kernelConfig) {
    final String clusterPrefix = prop("CLUSTER_PREFIX");
    final String clusterSuffix = prop("CLUSTER_SUFFIX");
    final int clusterStart = intProp("CLUSTER_START", -1);
    final int clusterEnd = intProp("CLUSTER_END", -1);
    final int self = getSelf(clusterPrefix, -1);

    for (int i = 0, n = kernelConfig.length(); i < n; i += 1) {
      final Item item = kernelConfig.getItem(i);
      final SpaceDef spaceDef = kernel.defineSpace(item);
      if (spaceDef != null) {
        if (spaceDef instanceof ActorSpaceDef) {
          ActorSpaceDef actorSpaceDef = (ActorSpaceDef) spaceDef;
          final HashTrieMap<Value, PartDef> partDefs =
                  partDefs(actorSpaceDef, self, clusterStart, clusterEnd, clusterPrefix, clusterSuffix);
          // add the mesh
          final ActorMeshDef defaultMeshDef = new ActorMeshDef(Uri.empty(), partDefs, UriMapper.empty(), UriMapper.empty(),
                  UriMapper.empty(), null, null, null, null);
          actorSpaceDef = actorSpaceDef.meshDef(defaultMeshDef);
          kernel.openSpace(actorSpaceDef);
          //System.out.println("Default Mesh Def " + defaultMeshDef);
        } else {
          kernel.openSpace(spaceDef);
        }
      }
    }
  }

  private static HashTrieMap<Value, PartDef> partDefs(ActorSpaceDef spaceDef, int self, int clusterStart, int clusterEnd,
                                                      String clusterPrefix, String clusterSuffix) {
    final String dbPath = prop("SWIM_DB_PATH");
    final int dpPageCacheSize = intProp("SWIM_DB_PAGE_CACHE_SIZE", 4096);

    System.out.println("Self: " + self);
    HashTrieMap<Value, PartDef> partDefs = HashTrieMap.empty();
    if (self != -1 && clusterStart != -1 && clusterEnd != -1 && !clusterPrefix.equals("")) {
      final long partitionEnd = 4294967294L;
      final long block =  (partitionEnd / (clusterEnd - clusterStart + 1));
      int index = 0;
      long lowerBound = 0;
      long upperBound = lowerBound + block;

      while (index <= (clusterEnd - clusterStart)) {
        final int key = clusterStart + index;

        for (NodeDef nodeDef : spaceDef.nodeDefs()) {
          final String hostUri = key == self ? "" : "warp://" + clusterPrefix + key + clusterSuffix;
          final PartDef partDef = makePartDef(nodeDef, hostUri, key, (int) lowerBound, (int) upperBound,
                  dbPath, dpPageCacheSize);
          partDefs = partDefs.updated(partDef.partKey(), partDef);
        }

        index += 1;
        lowerBound = upperBound + 1;
        upperBound = lowerBound + block;
      }
    }
    return partDefs;
  }

  private static PartDef makePartDef(NodeDef nodeDef, String hostUri, int key, int lowerBound, int upperBound,
                                     String dbPath, int dpPageCacheSize) {
    final PartPredicate partPredicate =
            PartPredicate.and(PartPredicate.node(nodeDef.nodePattern()), PartPredicate.hash(lowerBound, upperBound));
    final HostDef hostDef = ActorHostDef.fromHostUri(hostUri).isPrimary(true);
    final String nodeKey = nodeKey(nodeDef);
    final ActorPartDef actorPartDef = ActorPartDef.fromPartPredicate(Text.from(nodeKey + key), partPredicate)
            .hostDef(hostDef);
    if (dbPath == null || dbPath.trim().equals("")) {
      return actorPartDef;
    } else {
      final String storePath = dbPath + "/" + nodeKey;
      StoreDef storeDef = new DbStoreDef("", storePath, StoreSettings.standard().pageCacheSize(dpPageCacheSize));
      return actorPartDef.storeDef(storeDef);
    }
  }

  private static String nodeKey(NodeDef nodeDef) {
    final Collection<? extends AgentDef> agentDefs = nodeDef.agentDefs();
    final String def = nodeDef.nodePattern().toString();
    if (!agentDefs.isEmpty()) {
      return agentDefs.iterator().next().id().stringValue(def);
    } else {
      return def;
    }
  }

  private static int getSelf(String clusterPrefix, int def) {
    final String s = prop("HOSTNAME").trim();
    if (!clusterPrefix.equals("") && s.startsWith(clusterPrefix)) {
      final String s1 = s.substring(clusterPrefix.length());
      try {
        return Integer.parseInt(s1);
      } catch (NumberFormatException e) {
      }
    }
    return def;
  }

  private static String prop(String prop) {
    String s = System.getProperty(prop);
    if (s == null) {
      s = System.getenv(prop);
    }
    System.out.println("Property " + prop + "=" + s);
    return s == null ? "" : s;
  }

  private static int intProp(String prop, int def) {
    try {
      final String s = prop(prop);
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return def;
    }
  }

  private static void startAgencies(WarpRef warp) {
    final List<Agency> agencies = loadAgencies();
    for (Agency agency : agencies) {
      warp.command(agency.getUri(), "addInfo", agency.toValue());
    }
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {

    }
    NextBusHttpAPI.sendRoutes(agencies, warp);
  }

  private static List<Agency> loadAgencies() {
    final List<Agency> agencies = new ArrayList<>();
    InputStream is = null;
    Scanner scanner = null;
    try {
      is = TransitPlane.class.getResourceAsStream("/agencies.csv");
      scanner = new Scanner(is, "UTF-8");
      int index = 0;
      while (scanner.hasNextLine()) {
        final String[] line = scanner.nextLine().split(",");
        if (line.length >= 3) {
          agencies.add(new Agency(line[0], line[1], line[2], index++));
        }
      }
    } catch (Throwable t) {
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException ignore) {
      }
      if (scanner != null) {
        scanner.close();
      }
    }
    return agencies;
  }
}
