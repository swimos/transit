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
import swim.api.agent.AbstractAgent;
import swim.api.lane.CommandLane;
import swim.api.lane.JoinValueLane;
import swim.api.lane.MapLane;
import swim.api.lane.ValueLane;
import swim.structure.Record;
import swim.structure.Value;
import swim.transit.model.Agency;
import swim.uri.Uri;

public class CountryAgent extends AbstractAgent {
  @SwimLane("count")
  public ValueLane<Value> count;

  @SwimLane("agencies")
  public MapLane<String, Agency> agencies;

  @SwimLane("states")
  public MapLane<String, String> states;

  @SwimLane("stateCount")
  public MapLane<String, Integer> stateCount;

  @SwimLane("joinStateCount")
  public JoinValueLane<Value, Value> joinStateCount = this.<Value, Value>joinValueLane()
      .didUpdate(this::updateCounts);

  @SwimLane("speed")
  public ValueLane<Float> speed;

  @SwimLane("stateSpeed")
  public MapLane<String, Float> stateSpeed;

  @SwimLane("joinStateSpeed")
  public JoinValueLane<Value, Float> joinStateSpeed = this.<Value, Float>joinValueLane()
      .didUpdate(this::updateSpeeds);

  @SwimLane("addAgency")
  public CommandLane<Value> agencyAdd = this.<Value>commandLane().onCommand((Value value) -> {
    states.put(value.get("state").stringValue(), value.get("state").stringValue());
    joinStateCount.downlink(value.get("state")).nodeUri(Uri.parse(value.get("stateUri").stringValue()))
        .laneUri("count").open();
    joinStateSpeed.downlink(value.get("state")).nodeUri(Uri.parse(value.get("stateUri").stringValue()))
        .laneUri("speed").open();
    final Agency agency = Agency.form().cast(value);
    agencies.put(agency.getUri(), agency);

  });

  public void updateCounts(Value state, Value newCount, Value oldCount) {
    stateCount.put(state.stringValue(), newCount.get("current").intValue(0));
    int vCounts = 0;
    final Iterator<Integer> it = stateCount.valueIterator();
    while (it.hasNext()) {
      final Integer next = it.next();
      vCounts += next;
    }
    final int maxCount = Integer.max(count.get().get("max").intValue(0), vCounts);
    count.set(Record.create(2).slot("current", vCounts).slot("max", maxCount));
  }

  public void updateSpeeds(Value state, float newSpeed, float oldSpeed) {
    float vSpeeds = 0.0f;
    final Iterator<Float> it = joinStateSpeed.valueIterator();
    while (it.hasNext()) {
      final Float next = it.next();
      vSpeeds += next;
    }
    if (states.size() > 0) {
      speed.set(vSpeeds / states.size());
    }
    stateSpeed.put(state.stringValue(), newSpeed);
  }

  public void didStart() {
    System.out.println("Started Agent" + nodeUri().toString());
  }
}
