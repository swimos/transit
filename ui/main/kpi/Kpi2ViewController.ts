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

import { Value } from '@swim/structure';
import {NodeRef} from "@swim/client";
import {Color} from "@swim/color";
import {Transition} from "@swim/transition";
import {MapGraphicView} from "@swim/map";
// import {IntersectionMapView} from "../map/IntersectionMapView";
import {KpiViewController} from "./KpiViewController";
import {SliceView} from "@swim/pie";

export class Kpi2ViewController extends KpiViewController {
  /** @hidden */
  _nodeRef: NodeRef;
  /** @hidden */
  _trafficMapView: MapGraphicView;

  constructor(nodeRef: NodeRef, trafficMapView: MapGraphicView) {
    super();
    this._nodeRef = nodeRef;
    this._trafficMapView = trafficMapView;
  }

  get primaryColor(): Color {
    return Color.parse("#00a6ed");
  }

  updateKpi(): void {
    // console.info('Update KPI');
    // this.linkData();
    this.kpiTitle!.text('speed (km/h)');

  }

  updateData(key: Value, value: Value) {
    // console.log('updateData: k: ', key, ' v: ', value);
    const sliceColors = [Color.parse("#00a6ed"), Color.parse("#7ed321"),
                          Color.parse("#c200fb"), Color.parse("#50e3c2"),
                          Color.parse("#57b8ff"), Color.parse("#5aff15"),
                          Color.parse("#55dde0"), Color.parse("#f7aef8")];

    const tween: Transition<number> = Transition.duration(1000);
    const id = key.get("id").stringValue() || '';
    const index = key.get("index").numberValue() || 0;
    const sliceColor = sliceColors[index % 8];
    const sliceValue = value.numberValue() || 0;
    const pie = this.pieView;
    if (sliceValue > 0) {
      let slice: SliceView = pie.getChildView(id) as SliceView;
      if (slice) {
        slice.value(sliceValue, tween);
        // slice.label().text(sliceValue.toFixed());
      } else {
        slice = new SliceView()
            .value(sliceValue)
            .sliceColor(sliceColor)
            .label(sliceValue.toFixed())
            .legend(id);
        pie.setChildView(id, slice);
      }
    }
  }

  protected linkData() {
    // console.info('link data');
    this._nodeRef.downlinkMap()
      .nodeUri("/state/US/S-CA")
      .laneUri("agencySpeed")
      .didUpdate(this.updateData.bind(this))
      .open();
  }
}
