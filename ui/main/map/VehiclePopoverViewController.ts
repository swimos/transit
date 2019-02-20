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
import {PopoverView, PopoverViewController, HtmlView} from "@swim/view";
import {VehicleInfo} from "./VehicleModel";

export class VehiclePopoverViewController extends PopoverViewController {
  /** @hidden */
  _info: VehicleInfo;
  /** @hidden */
  _nodeRef: NodeRef;
  /** @hidden */
  _colorRage: string[];

  constructor(info: VehicleInfo, nodeRef: NodeRef) {
    super();
    this._info = info;
    this._nodeRef = nodeRef;
    this._colorRage = [
      '#00A6ED',
      '#7ED321',
      '#57B8FF',
      '#50E3C2',
      '#C200FB',
      '#5AFF15',
      '#55DDE0',
      '#F7AEF8'
    ]
  }

  didSetView(view: PopoverView): void {
    view.width(240)
        .height(360)
        .borderRadius(5)
        .padding(10)
        .backgroundColor(Color.parse("#071013").alpha(0.9))
        .backdropFilter("blur(2px)");

    const vehicle = this._info;
    const agencyIndex = vehicle.index%8;
    const colorAgency = this._colorRage[agencyIndex];
    //const agency = vehicle.agencyInfo!;

    // main container
    const busPopover = view.append("div").color(Color.parse("#ffffff").alpha(0.9));
    busPopover.className('busPopover');

    // header
    const placardHeader: HtmlView = busPopover.append("div").color(colorAgency);
    placardHeader.className('placardHeader');

    const ledIcon: HtmlView = placardHeader.append("div");
    ledIcon.className('ledIcon');
    ledIcon.backgroundColor(colorAgency);
    
    const ledLabel = ledIcon.append("h3").text(vehicle.routeTag);
    ledLabel.className('ledLabel');

    const placardLabel = placardHeader.append('h2');
    placardLabel.className('placardLabel');
    placardLabel.text(`bus #${vehicle.id}`);

    const popoverMeter = placardHeader.append("div");
    popoverMeter.className("popover-meter")
    popoverMeter.borderColor(colorAgency)

    const meterFill = popoverMeter.append("div");
    meterFill.className('fill');
    meterFill.backgroundColor(colorAgency)
      .height(`${(vehicle.speed/130)*100}%`);

    // add gap
    busPopover.append("div")
      .className("placardSubheader");

    // subheader
    const placardSubheader = busPopover.append("div");
    placardSubheader.className("placardSubheader");

    placardSubheader.append("div")
      .text(`${vehicle.speed} km/h`)
      .backgroundColor(colorAgency)
      .className("placardSubheaderItem");

    placardSubheader.append("div")
      .text(`${vehicle.dirId}`)
      .backgroundColor(colorAgency)
      .className("placardSubheaderItem");

    placardSubheader.append("div")
      .text(`${vehicle.heading}`)
      .backgroundColor(colorAgency)
      .className("placardSubheaderItem");

    busPopover.append("div").text(vehicle.routeTitle)
      .className("placard-route");

    busPopover.append("div").text(vehicle.agency)
      .paddingTop(10)
      .className("placard-route");

    // headerRow.append("span").key("routeTitle").text(vehicle.routeTitle);

    console.info('vehicle', vehicle);
    // TODO: layout popover
  }
}
