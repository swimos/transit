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

import {Length} from "@swim/length";
import {AnyColor, Color} from "@swim/color";
import {MemberAnimator} from "@swim/view";
import {MapCircleView} from "@swim/map";
import {VehicleMapViewController} from "./VehicleMapViewController";

export class VehicleMapView extends MapCircleView {
  /** @hidden */
  _viewController: VehicleMapViewController | null;

  constructor() {
    super();
    this.fill.setState(Color.transparent());
    this.radius.setState(Length.px(5));
  }

  get viewController(): VehicleMapViewController | null {
    return this._viewController;
  }

  @MemberAnimator(Color, "inherit")
  vehicleMarkerColor: MemberAnimator<this, Color, AnyColor>;
}
