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
import swim.api.agent.AgentType;
import swim.api.plane.AbstractPlane;
import swim.api.plane.PlaneContext;
import swim.api.ref.SwimRef;
import swim.api.server.ServerContext;
import swim.loader.ServerLoader;
import swim.transit.agent.AgencyAgent;
import swim.transit.agent.CountryAgent;
import swim.transit.agent.StateAgent;
import swim.transit.agent.VehicleAgent;
import swim.transit.model.Agency;

public class TransitPlane extends AbstractPlane {

  @SwimAgent(name = "country")
  @SwimRoute("/country/:id")
  final AgentType<?> transitAgent = agentClass(CountryAgent.class);

  @SwimAgent(name = "state")
  @SwimRoute("/state/:country/:state")
  final AgentType<?> stateAgent = agentClass(StateAgent.class);

  @SwimAgent(name = "agency")
  @SwimRoute("/agency/:country/:state/:id")
  final AgentType<?> agencyAgent = agentClass(AgencyAgent.class);

  @SwimAgent(name = "vehicle")
  @SwimRoute("/vehicle/:country/:state/:agency/:id")
  final AgentType<?> vehicleAgent = agentClass(VehicleAgent.class);

  public static void main(String[] args) throws IOException {
    final ServerContext server = ServerLoader.load(TransitPlane.class.getModule()).serverContext();
    final PlaneContext plane = server.getPlane("transit").planeContext();

    server.start();
    System.out.println("Running TransitPlane...");

    server.run(); // blocks until termination
    startAgencies(plane);
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
      is = TransitPlane.class.getModule().getResourceAsStream("/agencies.csv");
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
