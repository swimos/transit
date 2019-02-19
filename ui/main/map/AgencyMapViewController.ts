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

import {Value} from "@swim/structure";
import {MapDownlink, ValueDownlink, NodeRef} from "@swim/client";
import {Length} from "@swim/length";
import {Color} from "@swim/color";
import {Transition} from "@swim/transition";
import {PopoverView} from "@swim/view";
import {LngLat, MapProjection, MapGraphicViewController, MapCircleView} from "@swim/map";
import {AgencyInfo, AgencyBoundingBox} from "./AgencyModel";
import {AgencyMapView} from "./AgencyMapView";
import {AgencyPopoverViewController} from "./AgencyPopoverViewController";
import {VehicleInfo} from "./VehicleModel";
import {VehicleMapView} from "./VehicleMapView";
import {VehicleMapViewController} from "./VehicleMapViewController";

const VEHICLE_ZOOM = 11;

export class AgencyMapViewController extends MapGraphicViewController<AgencyMapView> {
  /** @hodden */
  _info: AgencyInfo;
  /** @hidden */
  _nodeRef: NodeRef;
  /** @hidden */
  _boundingBoxLink: ValueDownlink<Value> | null;
  /** @hidden */
  _vehiclesLink: MapDownlink<Value, Value> | null;
  /** @hidden */
  _popoverView: PopoverView | null;
  /** @hidden*/
  _reduced: boolean | undefined;

  constructor(info: AgencyInfo, nodeRef: NodeRef) {
    super();
    this._info = info;
    this._nodeRef = nodeRef;
    this._boundingBoxLink = null;
    this._vehiclesLink = null;
    this._popoverView = null;
  }

  protected didSetBoundingBox(value: Value): void {
    //console.log("agency " + this._info.id + " didSetBoundingBox:", value.toAny());
    if (this._reduced === true) {
      this.ripple(this._view!.agencyMarkerColor.value!);
    }
    this.animate();
  }

  protected didUpdateVehicle(key: Value, value: Value): void {
    const vehicleInfo = value.toAny() as unknown as VehicleInfo;
    const vehicleId = "" + vehicleInfo.id;
    vehicleInfo.agencyInfo = this._info;
    //console.log("didUpdateVehicle:", vehicleInfo);

    let vehicleMapView = this.getChildView(vehicleId) as VehicleMapView | null;
    if (!vehicleMapView) {
      const vehicleNodeUri = key.stringValue()!;
      const vehicleNodeRef = this._nodeRef.nodeRef(vehicleNodeUri);

      vehicleMapView = new VehicleMapView();
      const vehcileMapViewController = new VehicleMapViewController(vehicleInfo, vehicleNodeRef);
      vehicleMapView.setViewController(vehcileMapViewController);
      this.setChildView(vehicleId, vehicleMapView);
    } else {
      const vehicleMapViewController = vehicleMapView.viewController!;
      vehicleMapViewController.setInfo(vehicleInfo);
    }
  }

  protected initMarkerView(): void {
    let markerView = this.getChildView("marker") as MapCircleView | null;
    if (!markerView && this._boundingBoxLink) {
      const boundingBox = this._boundingBoxLink.get().toAny() as unknown as AgencyBoundingBox | undefined;
      if (boundingBox) {
        const lng = (boundingBox.minLng + boundingBox.maxLng) / 2;
        const lat = (boundingBox.minLat + boundingBox.maxLat) / 2;
        markerView = new MapCircleView();
        markerView.center.setState(new LngLat(lng, lat));
        markerView.radius.setState(Length.px(10));
        markerView.fill.setState(this._view!.agencyMarkerColor.value!);
        markerView.on("click", this.onMarkerClick.bind(this));
        this.setChildView("marker", markerView);
      }
    }
  }

  protected onMarkerClick(event: MouseEvent): void {
    event.stopPropagation();
    if (!this._popoverView) {
      this._popoverView = new PopoverView();
      const popoverViewController = new AgencyPopoverViewController(this._info, this._nodeRef);
      this._popoverView.setViewController(popoverViewController);
      this._popoverView.hidePopover();
    }
    this._popoverView.setSource(this.getChildView("marker"));
    this.appView!.togglePopover(this._popoverView, {multi: event.altKey});
  }

  protected ripple(color: Color): void {
    if (document.hidden || this.culled || !this._boundingBoxLink) {
      return;
    }
    const boundingBox = this._boundingBoxLink.get().toAny() as unknown as AgencyBoundingBox;
    const lng = (boundingBox.minLng + boundingBox.maxLng) / 2;
    const lat = (boundingBox.minLat + boundingBox.maxLat) / 2;
    const ripple = new MapCircleView()
        .center(new LngLat(lng, lat))
        .radius(0)
        .fill(null)
        .stroke(color.alpha(0.25))
        .strokeWidth(1);
    this.appendChildView(ripple);
    const radius = Math.min(this.bounds.width, this.bounds.height) / 8;
    const tween = Transition.duration<any>(2000);
    ripple.stroke(color.alpha(0), tween)
          .radius(radius, tween.onEnd(function () { ripple.remove(); }));
  }

  viewDidMount(view: AgencyMapView): void {
    this.linkBoundingBox();
  }

  viewWillUnmount(view: AgencyMapView): void {
    this.unlinkBoundingBox();
    this.unlinkVehicles();
  }

  viewDidSetProjection(projection: MapProjection, view: AgencyMapView): void {
    const boundingBox = this._boundingBoxLink!.get().toAny() as unknown as AgencyBoundingBox | undefined;
    if (boundingBox) {
      const [sw, ne] = projection.bounds;
      const culled = !(boundingBox.minLng <= ne.lng && sw.lng <= boundingBox.maxLng
                    && boundingBox.minLat <= ne.lat && sw.lat <= boundingBox.maxLat);
      view.setCulled(culled);
    } else {
      view.setCulled(true);
    }
  }

  viewDidCull(view: AgencyMapView): void {
    this.updateLevelOfDetail();
  }

  protected updateLevelOfDetail(): void {
    if (this._reduced !== false && !this.culled && this.zoom >= VEHICLE_ZOOM) {
      this._reduced = false;
      this.removeAll();
      this.linkVehicles();
    } else if (this._reduced !== true && !this.culled && this.zoom < VEHICLE_ZOOM) {
      this._reduced = true;
      this.unlinkVehicles();
      this.removeAll();
      this.initMarkerView();
    }
  }

  protected linkBoundingBox(): void {
    if (!this._boundingBoxLink) {
      this._boundingBoxLink = this._nodeRef.downlinkValue()
          .laneUri("boundingBox")
          .didSet(this.didSetBoundingBox.bind(this))
          .open();
    }
  }

  protected unlinkBoundingBox(): void {
    if (this._boundingBoxLink) {
      this._boundingBoxLink.close();
      this._boundingBoxLink = null;
    }
  }

  protected linkVehicles(): void {
    if (!this._vehiclesLink) {
      //console.log("agency " + this._info.id + " linkVehicles");
      this._vehiclesLink = this._nodeRef.downlinkMap()
          .laneUri("vehicles")
          .didUpdate(this.didUpdateVehicle.bind(this))
          .open();
    }
  }

  protected unlinkVehicles(): void {
    if (this._vehiclesLink) {
      //console.log("agency " + this._info.id + " unlinkVehicles");
      this._vehiclesLink.close();
      this._vehiclesLink = null;
    }
  }
}
