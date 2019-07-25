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
import swim.api.lane.ValueLane;
import swim.transit.model.Vehicle;

public class VehicleAgent extends AbstractAgent {
  private long lastReportedTime = 0L;

  @SwimLane("vehicle")
  public ValueLane<Vehicle> vehicle;

  @SwimTransient
  @SwimLane("speeds")
  public MapLane<Long, Integer> speeds;

  @SwimTransient
  @SwimLane("accelerations")
  public MapLane<Long, Integer> accelerations;

  @SwimLane("addVehicle")
  public CommandLane<Vehicle> addVehicle = this.<Vehicle>commandLane().onCommand(this::onVehicle);

  private void onVehicle(Vehicle v) {
    final long time = System.currentTimeMillis() - (v.getSecsSinceReport() * 1000L);
    final int oldSpeed = vehicle.get() != null ? vehicle.get().getSpeed() : 0;
    this.vehicle.set(v);
    speeds.put(time, v.getSpeed());
    if (speeds.size() > 10) {
      speeds.drop(speeds.size() - 10);
    }
    if (lastReportedTime > 0) {
      final float acceleration = (float) ((v.getSpeed() - oldSpeed)) / (time - lastReportedTime) * 3600;
      accelerations.put(time, Math.round(acceleration));
      if (accelerations.size() > 10) {
        accelerations.drop(accelerations.size() - 10);
      }
    }
    lastReportedTime = time;
  }

  @Override
  public void didStart() {
    //System.out.println("Started Agent: " + nodeUri().toString());
  }
}
