// Copyright 2015-2019 SWIM.AI inc.
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
import java.util.List;
import java.util.Scanner;
import swim.api.SwimAgent;
import swim.api.SwimRoute;
import swim.api.agent.AgentRoute;
import swim.api.plane.AbstractPlane;
import swim.api.ref.SwimRef;
import swim.fabric.Fabric;
import swim.kernel.Kernel;
import swim.server.ServerLoader;
import swim.transit.agent.AgencyAgent;
import swim.transit.agent.CountryAgent;
import swim.transit.agent.StateAgent;
import swim.transit.agent.VehicleAgent;
import swim.transit.model.Agency;

public class TransitPlane extends AbstractPlane {
  @SwimAgent("country")
  @SwimRoute("/country/:id")
  AgentRoute<CountryAgent> transitAgent;

  @SwimAgent("state")
  @SwimRoute("/state/:country/:state")
  AgentRoute<StateAgent> stateAgent;

  @SwimAgent("agency")
  @SwimRoute("/agency/:country/:state/:id")
  AgentRoute<AgencyAgent> agencyAgent;

  @SwimAgent("vehicle")
  @SwimRoute("/vehicle/:country/:state/:agency/:id")
  AgentRoute<VehicleAgent> vehicleAgent;

  public static void main(String[] args) {
    final Kernel kernel = ServerLoader.loadServer();
    final Fabric fabric = (Fabric) kernel.getSpace("transit");

    kernel.start();
    System.out.println("Running TransitPlane...");

    startAgencies(fabric);

    kernel.run(); // blocks until termination
  }

  private static void startAgencies(SwimRef swim) {
    final List<Agency> agencies = loadAgencies();
    for (Agency agency : agencies) {
      swim.command(agency.getUri(), "addInfo", agency.toValue());
    }
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {

    }
    final NextBusHttpAPI nextBusHttpAPI = new NextBusHttpAPI(swim);
    nextBusHttpAPI.sendRoutes(agencies);
    nextBusHttpAPI.repeatSendVehicleInfo(agencies);
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
