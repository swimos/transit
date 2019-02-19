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

import java.util.Objects;
import swim.structure.Form;
import swim.structure.Kind;
import swim.structure.Tag;
import swim.structure.Value;

@Tag("agency")
public class Agency {

  private String id = "";
  private String state = "";
  private String country = "";
  private int index = 0;

  public Agency() {
  }

  public Agency(String id, String state, String country, int index) {
    this.id = id;
    this.state = state;
    this.country = country;
    this.index = index;
  }

  public String getId() {
    return id;
  }

  public String getState() {
    return state;
  }

  public String getCountry() {
    return country;
  }

  public int getIndex() {
    return index;
  }

  public String getUri() {
    return "/agency/" + getCountry() + "/" + getState() + "/" + getId();
  }

  public Value toValue() {
    return form().mold(this).toValue();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof Agency) {
      final Agency that = (Agency) other;
      return Objects.equals(id, that.id)
          && Objects.equals(state, that.state)
          && Objects.equals(country, that.country)
          && Objects.equals(index, that.index);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, state, country, index);
  }

  @Override
  public String toString() {
    return "Agency{"
        + "id='" + id + '\''
        + ", state='" + state + '\''
        + ", country='" + country + '\''
        + ", index=" + index
        + '}';
  }

  @Kind
  private static Form<Agency> form;

  public static Form<Agency> form() {
    if (form == null) {
      form = Form.forClass(Agency.class);
    }
    return form;
  }
}
