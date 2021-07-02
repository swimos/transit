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

public class SphericalMercator {

  private SphericalMercator() {
    // static
  }

  public static final double MAX_LAT = Math.atan(Math.sinh(Math.PI));

  public static double projectLng(double lng) {
    return SphericalMercator.scale(Math.toRadians(lng));
  }

  public static double projectLat(double lat) {
    return SphericalMercator.scale(Math.log(Math.tan(Math.PI / 4.0 + Math.min(Math.max(-MAX_LAT, Math.toRadians(lat)), MAX_LAT) / 2.0)));
  }

  static double scale(double x) {
    return (Math.min(Math.max(-Math.PI, x), Math.PI) + Math.PI) / (Math.PI * 2.0);
  }

  public static double unprojectX(double x) {
    return SphericalMercator.round(Math.toDegrees(SphericalMercator.unscale(x)));
  }

  public static double unprojectY(double y) {
    return SphericalMercator.round(Math.toDegrees(Math.atan(Math.exp(SphericalMercator.unscale(y))) * 2.0 - Math.PI / 2.0));
  }

  static double unscale(double x) {
    return x * (Math.PI * 2.0) - Math.PI;
  }

  static double round(double value) {
    return (double) Math.round(value * 100000000.0) / 100000000.0;
  }

}
