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

package swim.transit.agent;

import swim.api.SwimLane;
import swim.api.SwimTransient;
import swim.api.agent.AbstractAgent;
import swim.api.lane.CommandLane;
import swim.api.lane.MapLane;
import swim.structure.Value;
import swim.transit.model.Vehicle;
import swim.uri.Uri;
import swim.uri.UriPath;

public class MapTileAgent extends AbstractAgent {

  int tileX;
  int tileY;
  int tileZ;

  Uri parentTileUri;

  @SwimTransient
  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles;

  @SwimLane("updateVehicle")
  public CommandLane<Vehicle> updateVehicle = this.<Vehicle>commandLane().onCommand((Vehicle vehicle) -> {
    //System.out.println(nodeUri() + " updateVehicle: " + vehicle.getUri());
    this.vehicles.put(vehicle.getUri(), vehicle);
  });

  @SwimLane("removeVehicle")
  public CommandLane<String> removeVehicle = this.<String>commandLane().onCommand((String vehicleUri) -> {
    //System.out.println(nodeUri() + " removeVehicle: " + vehicleUri);
    this.vehicles.remove(vehicleUri);
  });

  @Override
  public void didStart() {
    final String[] coordinates = nodeUri().path().foot().toString().split(",");
    this.tileX = Integer.parseInt(coordinates[0]);
    this.tileY = Integer.parseInt(coordinates[1]);
    this.tileZ = Integer.parseInt(coordinates[2]);
    final int parentTileX = this.tileX / 2;
    final int parentTileY = this.tileY / 2;
    final int parentTileZ = this.tileZ - 1;
    this.parentTileUri = Uri.create(UriPath.of("/", "map", "/", parentTileX + "," + parentTileY + "," + parentTileZ));
    System.out.println("Started MapTileAgent x: " + this.tileX + "; y: " + this.tileY + "; z: " + this.tileZ);
  }

}
