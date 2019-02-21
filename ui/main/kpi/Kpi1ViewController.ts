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

// import { Value } from '@swim/structure';
import {NodeRef} from "@swim/client";
import {Color} from "@swim/color";
// import {Transition} from "@swim/transition";
import {MapGraphicView} from "@swim/map";
//import {AgencyMapView} from "../map/AgencyMapView";
import {KpiViewController} from "./KpiViewController";

export class Kpi1ViewController extends KpiViewController {
  /** @hidden */
  _nodeRef: NodeRef;
  /** @hidden */
  _transitMapView: MapGraphicView;

  constructor(nodeRef: NodeRef, transitMapView: MapGraphicView) {
    super();
    this._nodeRef = nodeRef;
    this._transitMapView = transitMapView;
  }

  get primaryColor(): Color {
    return Color.parse("#00a6ed");
  }

  updateKpi(): void {
    this.kpiTitle!.text('speed (km/h)');
    // let meterValue = 0;
    // let spaceValue = 0;
    //const agencyMapViews = this._transitMapView.childViews;
    //console.log('agencyMapViews: ', agencyMapViews);
    //for (let i = 0; i < agencyMapViews.length; i += 1) {
    //  const agencyMapView = agencyMapViews[i];
    //  if (agencyMapView instanceof AgencyMapView ) {
    //    const agencyMapViewController = agencyMapView.viewController!;
    //    //console.log('agencyMapViewController: ', agencyMapViewController);
    //     if (intersectionMapViewController._pedCall) {
    //       meterValue += 1;
    //     } else {
    //       spaceValue += 1;
    //     }
    //  }
    //}

    // const title = this.titleView;
    // const meter = this.meterView;
    // const empty = this.emptyView;
    // const tween = Transition.duration<any>(1000);

    // this.title!.text('Palo Alto - PEDESTRIAN BACKUP');
    // this.subtitle!.text('@ CROSSWALKS');

    // meter.value(meterValue, tween);
    // empty.value(spaceValue, tween);
    // this.meterLegend!.text("Waiting (" + meterValue + ")");
    // this.clearLegend!.text("Clear (" + spaceValue + ")");
    // title.text(Math.round(100 * meterValue / ((meterValue + spaceValue) || 1)) + "%");
  }

}
