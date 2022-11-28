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

import {AnyValue, Value} from "@swim/structure";
import {MapDownlink, NodeRef} from "@swim/client";
import {Color} from "@swim/color";
import {PopoverView, PopoverViewController, HtmlView} from "@swim/view";
import {VehicleInfo} from "./VehicleModel";
import {ChartView, LineGraphView} from "@swim/chart";

export class VehiclePopoverViewController extends PopoverViewController {
  /** @hidden */
  _info: VehicleInfo;
  /** @hidden */
  _nodeRef: NodeRef;
  /** @hidden */
  _colorRage: string[];
  /** @hidden */
  _speedChart: ChartView;
  /** @hidden */
  _linkSpeedHistory?: MapDownlink<Value, Value, AnyValue, AnyValue>;
  /** @hidden */
  _speedPlot: any;
  /** @hidden */
  _linkAccelHistory?: MapDownlink<Value, Value, AnyValue, AnyValue>;
  /** @hidden */
  _accelerationPlot: any;
  /** @hidden */
  _speedItem: any;

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

    // speed
    this._speedItem = placardSubheader.append("div")
      .text(`${vehicle.speed} km/h`)
      .backgroundColor(colorAgency);
    this._speedItem.className("placardSubheaderItem");

    // direction
    placardSubheader.append("div")
      .text(`${vehicle.dirId}`)
      .backgroundColor(colorAgency)
      .className("placardSubheaderItem");

    // heading
    placardSubheader.append("div")
      .text(`${vehicle.heading}`)
      .backgroundColor(colorAgency)
      .className("placardSubheaderItem");

    // route name
    busPopover.append("div").text(vehicle.routeTitle)
      .className("placard-route");

    // agency name
    busPopover.append("div").text(vehicle.agency)
      .paddingTop(10)
      .className("placard-agency");

    // start charts
    const chartsContainer = busPopover.append("div");
    chartsContainer.className('busCharts');

    // speed chart title
    const speedChartTitle = chartsContainer.append("h3").text("Speed");
    speedChartTitle.className("chartTitle");

    // chart container
    const speedChartContainer = chartsContainer.append("div");
    speedChartContainer.className('chartContainer');

    // speed chart
    const speedChartCanvas = speedChartContainer.append("canvas").height(200);
    this._speedChart = new ChartView()
      .bottomAxis("time")
      .leftAxis("linear")
      .bottomGesture(false)
      .leftDomainPadding([0.1, 0.1])
      .topGutter(0)
      .rightGutter(0)
      .bottomGutter(0)
      .leftGutter(-1)
      .domainColor(Color.transparent(0))
      .tickMarkColor(Color.transparent(0));
    
    speedChartCanvas.append(this._speedChart);    

    this._speedPlot = new LineGraphView()
      .stroke(colorAgency)
      .strokeWidth(1);

    this._speedChart.addPlot(this._speedPlot);    
    
    // acceleration chart title
    const accelChartTitle = chartsContainer.append("h3").text("Acceleration");
    accelChartTitle.className("chartTitle");

    // chart container
    const accelChartContainer = chartsContainer.append("div");
    accelChartContainer.className('chartContainer');

    // speed chart
    const accelChartCanvas = accelChartContainer.append("canvas").height(200);
    const accelerationChart = new ChartView()
      .bottomAxis("time")
      .leftAxis("linear")
      .bottomGesture(false)
      .leftDomainPadding([0.1, 0.1])
      .topGutter(0)
      .rightGutter(0)
      .bottomGutter(0)
      .leftGutter(-1)
      .domainColor(Color.transparent(0))
      .tickMarkColor(Color.transparent(0));
    
      accelChartCanvas.append(accelerationChart);    

    this._accelerationPlot = new LineGraphView()
      .stroke(colorAgency)
      .strokeWidth(1);

    accelerationChart.addPlot(this._accelerationPlot);       

    // headerRow.append("span").key("routeTitle").text(vehicle.routeTitle);

    //console.info('vehicle', vehicle);
    // TODO: layout popover
  }

  popoverDidShow(view: any): void {
    this.linkSpeedHistory();
    this.linkAccelHistory();
  }

  popoverDidHide(view: any): void {
    this.unlinkSpeedHistory();
    this.unlinkAccelHistory();
  }  

  // speed history data handlers
  protected linkSpeedHistory() {
    if(!this._linkSpeedHistory) {
      this._linkSpeedHistory = this._nodeRef.downlinkMap()
        .nodeUri(this._info.uri)
        .laneUri("speeds")
        .didUpdate(this.didUpdateSpeedHistory.bind(this))
        .didRemove(this.didRemoveSpeedHistory.bind(this))
        .open();
    }
  }

  protected unlinkSpeedHistory() {
    if (this._linkSpeedHistory) {
      this._linkSpeedHistory.close();
      this._linkSpeedHistory = undefined;
    }
  }  

  didUpdateSpeedHistory(k: Value, v: Value) {
    this._speedItem.text(`${v.numberValue()} km/h`)
    this._speedPlot.insertDatum({x: k.numberValue(), y: v.numberValue()});
  }

  didRemoveSpeedHistory(k: Value, v: Value) {
    this._speedPlot.removeDatum({x: k.numberValue(), y: v.numberValue()});
  }  

  // acceleration history data handlers
  protected linkAccelHistory() {
    if(!this._linkAccelHistory) {
      this._linkAccelHistory = this._nodeRef.downlinkMap()
        .nodeUri(this._info.uri)
        .laneUri("accelerations")
        .didUpdate(this.didUpdateAccelHistory.bind(this))
        .didRemove(this.didRemoveAccelHistory.bind(this))
        .open();
    }
  }

  protected unlinkAccelHistory() {
    if (this._linkAccelHistory) {
      this._linkAccelHistory.close();
      this._linkAccelHistory = undefined;
    }
  }  

  didUpdateAccelHistory(k: Value, v: Value) {
    this._accelerationPlot.insertDatum({x: k.numberValue(), y: v.numberValue()});
  }

  didRemoveAccelHistory(k: Value, v: Value) {
    this._accelerationPlot.removeDatum({x: k.numberValue(), y: v.numberValue()});
  }    
}
