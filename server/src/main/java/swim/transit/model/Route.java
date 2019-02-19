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

import swim.structure.Form;
import swim.structure.Kind;
import swim.structure.Tag;

@Tag("route")
public class Route {

  private String tag = "";
  private String title = "";

  public Route() {
  }

  public Route(String tag, String title) {
    this.tag = tag;
    this.title = title;
  }

  public String getTag() {
    return tag;
  }

  public Route withTag(String tag) {
    return new Route(tag, title);
  }

  public String getTitle() {
    return title;
  }

  public Route withTitle(String title) {
    return new Route(tag, title);
  }

  @Kind
  private static Form<Route> form;
}
