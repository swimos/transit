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

import java.util.Iterator;
import swim.api.SwimLane;
import swim.api.SwimTransient;
import swim.api.agent.AbstractAgent;
import swim.api.lane.CommandLane;
import swim.api.lane.JoinMapLane;
import swim.api.lane.JoinValueLane;
import swim.api.lane.MapLane;
import swim.api.lane.ValueLane;
import swim.structure.Record;
import swim.structure.Value;
import swim.transit.model.Agency;
import swim.transit.model.Vehicle;

public class StateAgent extends AbstractAgent {
  @SwimLane("count")
  public ValueLane<Value> count;

  @SwimTransient
  @SwimLane("agencyCount")
  public MapLane<Agency, Integer> agencyCount;

  @SwimLane("joinAgencyCount")
  public JoinValueLane<Agency, Integer> joinAgencyCount = this.<Agency, Integer>joinValueLane()
      .didUpdate(this::updateCounts);

  @SwimTransient
  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles;

  @SwimLane("joinAgencyVehicles")
  public JoinMapLane<Agency, String, Vehicle> joinAgencyVehicles = this.<Agency, String, Vehicle>joinMapLane()
      .didUpdate((String key, Vehicle newEntry, Vehicle oldEntry) -> vehicles.put(key, newEntry))
      .didRemove((String key, Vehicle vehicle) -> vehicles.remove(key));

  @SwimLane("speed")
  public ValueLane<Float> speed;

  @SwimTransient
  @SwimLane("agencySpeed")
  public MapLane<Agency, Float> agencySpeed;

  @SwimLane("joinStateSpeed")
  public JoinValueLane<Agency, Float> joinAgencySpeed = this.<Agency, Float>joinValueLane()
      .didUpdate(this::updateSpeeds);

  @SwimLane("addAgency")
  public CommandLane<Agency> agencyAdd = this.<Agency>commandLane().onCommand((Agency agency) -> {
    joinAgencyCount.downlink(agency).nodeUri(agency.getUri()).laneUri("count").open();
    joinAgencyVehicles.downlink(agency).nodeUri(agency.getUri()).laneUri("vehicles").open();
    joinAgencySpeed.downlink(agency).nodeUri(agency.getUri()).laneUri("speed").open();
    context.command("/country/" + getProp("country").stringValue(), "addAgency",
        agency.toValue().unflattened().slot("stateUri", nodeUri().toString()));
  });

  public void updateCounts(Agency agency, int newCount, int oldCount) {
    int vCounts = 0;
    final Iterator<Integer> it = joinAgencyCount.valueIterator();
    while (it.hasNext()) {
      final Integer next = it.next();
      vCounts += next;
    }

    final int maxCount = Integer.max(count.get().get("max").intValue(0), vCounts);
    count.set(Record.create(2).slot("current", vCounts).slot("max", maxCount));
    agencyCount.put(agency, newCount);
  }

  public void updateSpeeds(Agency agency, float newSpeed, float oldSpeed) {
    float vSpeeds = 0.0f;
    final Iterator<Float> it = joinAgencySpeed.valueIterator();
    while (it.hasNext()) {
      final Float next = it.next();
      vSpeeds += next;
    }
    if (joinAgencyCount.size() > 0) {
      speed.set(vSpeeds / joinAgencyCount.size());
    }
    agencySpeed.put(agency, newSpeed);
  }

  public void didStart() {
    vehicles.clear();
    agencyCount.clear();
    agencySpeed.clear();
    System.out.println("Started Agent" + nodeUri().toString());
  }
}
