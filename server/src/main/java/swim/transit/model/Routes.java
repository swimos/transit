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

package swim.transit.model;

import java.util.ArrayList;
import java.util.List;
import swim.structure.Form;
import swim.structure.Kind;
import swim.structure.Tag;

@Tag("Routes")
public class Routes {

  private final List<Route> routes = new ArrayList<Route>();

  public Routes() {
  }

  public List<Route> getRoutes() {
    return routes;
  }

  public void add(Route route) {
    routes.add(route);
  }

  @Kind
  private static Form<Routes> form;
}
