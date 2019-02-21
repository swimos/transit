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

import {NodeRef} from "@swim/client";
import {Color} from "@swim/color";
import {Transition} from "@swim/transition";
import {PopoverView} from "@swim/view";
import {LngLat, MapGraphicViewController, MapCircleView} from "@swim/map";
import {VehicleInfo} from "./VehicleModel";
import {VehicleMapView} from "./VehicleMapView";
import {VehiclePopoverViewController} from "./VehiclePopoverViewController";

export class VehicleMapViewController extends MapGraphicViewController<VehicleMapView> {
  /** @hodden */
  _info: VehicleInfo;
  /** @hidden */
  _nodeRef: NodeRef;
  /** @hidden */
  _popoverView: PopoverView | null;

  constructor(info: VehicleInfo, nodeRef: NodeRef) {
    super();
    this._info = info;
    this._nodeRef = nodeRef;
  }

  setInfo(info: VehicleInfo): void {
    this._info = info;
    this.updateVehicle();
  }

  updateVehicle(): void {
    const view = this._view!;
    const info = this._info;
    if (info.longitude && info.latitude) {
      const oldCenter = view.center.value!;
      const newCenter = new LngLat(info.longitude, info.latitude);
      if (!oldCenter.equals(newCenter)) {
        view.center.setState(newCenter, Transition.duration(10000));
        this.ripple(this._view!.vehicleMarkerColor.value!);
      }
    }
    view.fill(view.vehicleMarkerColor.value!, Transition.duration(500));
  }

  didSetView(view: VehicleMapView): void {
    view.on("click", this.onClick.bind(this));

    const info = this._info;
    if (info.longitude && info.latitude) {
      this._view!.center.setState(new LngLat(info.longitude, info.latitude));
    }
    view.fill(view.vehicleMarkerColor.value!);
  }

  protected ripple(color: Color): void {
    const info = this._info;
    if (document.hidden || this.culled) {
      return;
    }
    const ripple = new MapCircleView()
        .center(new LngLat(info.longitude, info.latitude))
        .radius(0)
        .fill(null)
        .stroke(color.alpha(1))
        .strokeWidth(1);
    this.appendChildView(ripple);
    const radius = Math.min(this.bounds.width, this.bounds.height) / 8;
    const tween = Transition.duration<any>(5000);
    ripple.stroke(color.alpha(0), tween)
          .radius(radius, tween.onEnd(function () { ripple.remove(); }));
  }

  protected onClick(event: MouseEvent): void {
    event.stopPropagation();
    if (!this._popoverView) {
      this._popoverView = new PopoverView();
      const popoverViewController = new VehiclePopoverViewController(this._info, this._nodeRef);
      this._popoverView.setViewController(popoverViewController);
      this._popoverView.setSource(this._view!);
      this._popoverView.hidePopover();
    }
    this.appView!.togglePopover(this._popoverView, {multi: event.altKey});
  }
}
