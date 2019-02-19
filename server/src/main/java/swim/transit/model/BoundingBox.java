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

@Tag("bounds")
public class BoundingBox {

  float minLng = Float.MAX_VALUE;
  float minLat = Float.MAX_VALUE;
  float maxLng = Float.MIN_VALUE;
  float maxLat = Float.MIN_VALUE;

  public BoundingBox() {
  }

  public BoundingBox(float minLng, float minLat, float maxLng, float maxLat) {
    this.minLat = minLat;
    this.minLng = minLng;
    this.maxLat = maxLat;
    this.maxLng = maxLng;
  }

  public float getMinLat() {
    return minLat;
  }

  public float getMinLng() {
    return minLng;
  }

  public float getMaxLat() {
    return maxLat;
  }

  public float getMaxLng() {
    return maxLng;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final BoundingBox that = (BoundingBox) o;
    return Float.compare(that.minLat, minLat) == 0
        && Float.compare(that.minLng, minLng) == 0
        && Float.compare(that.maxLat, maxLat) == 0
        && Float.compare(that.maxLng, maxLng) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(minLat, minLng, maxLat, maxLng);
  }

  @Override
  public String toString() {
    return "BoundingBox{"
        + "minLng=" + minLng
        + ", minLat=" + minLat
        + ", maxLng=" + maxLng
        + ", maxLat=" + maxLat
        + '}';
  }

  @Kind
  private static Form<BoundingBox> form;
}
