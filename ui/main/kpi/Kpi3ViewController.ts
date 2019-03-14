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
import {MapGraphicView} from "@swim/map";
import {KpiViewController} from "./KpiViewController";

export class Kpi3ViewController extends KpiViewController {
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
    this.kpiTitle!.text('Total Count');
  }

  updateData(k: Value, v: Value) {
    // console.log('k: ', k, ' v: ', v);
  }

  protected linkData() {
    this._nodeRef.downlinkMap()
      .laneUri("count")
      .didUpdate(this.updateData.bind(this))
      .open();
  }
}
