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

package swim.transit.model;

import java.util.HashMap;
import java.util.Map;
import swim.structure.Form;
import swim.structure.Kind;
import swim.structure.Tag;

@Tag("vehicles")
public class Vehicles {

  private final Map<String, Vehicle> vehicles = new HashMap<String, Vehicle>();

  public Vehicles() {
  }

  public Map<String, Vehicle> getVehicles() {
    return vehicles;
  }

  public void add(Vehicle vehicle) {
    vehicles.put(vehicle.getUri(), vehicle);
  }

  @Kind
  private static Form<Vehicles> form;
}
