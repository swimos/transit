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

import {AnyColor, Color} from "@swim/color";
import {MemberAnimator} from "@swim/view";
import {MapGraphicView} from "@swim/map";
import {TransitMapViewController} from "./TransitMapViewController";

export class TransitMapView extends MapGraphicView {
  /** @hidden */
  _viewController: TransitMapViewController | null;

  constructor() {
    super();
    this.agencyMarkerColor.setState(Color.parse("#5aff15"));
    this.vehicleMarkerColor.setState(Color.parse("#00a6ed"));
  }

  get viewController(): TransitMapViewController | null {
    return this._viewController;
  }

  @MemberAnimator(Color)
  agencyMarkerColor: MemberAnimator<this, Color, AnyColor>;

  @MemberAnimator(Color)
  vehicleMarkerColor: MemberAnimator<this, Color, AnyColor>;
}
