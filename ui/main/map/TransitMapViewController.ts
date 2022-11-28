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
import {MapGraphicViewController} from "@swim/map";
import {TransitMapView} from "./TransitMapView";
import {AgencyInfo} from "./AgencyModel";
import {AgencyMapView} from "./AgencyMapView";
import {AgencyMapViewController} from "./AgencyMapViewController";

export class TransitMapViewController extends MapGraphicViewController<TransitMapView> {
  /** @hidden */
  _nodeRef: NodeRef;
  /** @hidden */
  _agenciesLink: MapDownlink<Value, Value, AnyValue, AnyValue> | null;

  constructor(nodeRef: NodeRef) {
    super();
    this._nodeRef = nodeRef;
    this._agenciesLink = null;
  }

  viewDidMount(view: TransitMapView): void {
    this.linkAgencies();
  }

  viewWillUnmount(view: TransitMapView): void {
    this.unlinkAgencies();
  }

  protected linkAgencies(): void {
    if (!this._agenciesLink) {
      this._agenciesLink = this._nodeRef.downlinkMap()
          .laneUri("agencies")
          .didUpdate(this.didUpdateAgency.bind(this))
          .open();
    }
  }

  protected unlinkAgencies(): void {
    if (this._agenciesLink) {
      this._agenciesLink.close();
      this._agenciesLink = null;
    }
  }

  protected didUpdateAgency(key: Value, value: Value): void {
    const agencyInfo = value.toAny() as unknown as AgencyInfo;
    const agencyId = "" + agencyInfo.id;
    //console.log("didUpdateAgenc:", agencyInfo);

    let agencyMapView = this.getChildView(agencyId);
    if (!agencyMapView) {
      const agencyNodeUri = key.stringValue()!;
      const agencyNodeRef = this._nodeRef.nodeRef(agencyNodeUri);

      agencyMapView = new AgencyMapView();
      const agencyMapViewController = new AgencyMapViewController(agencyInfo, agencyNodeRef);
      agencyMapView.setViewController(agencyMapViewController);
      this.setChildView(agencyId, agencyMapView);
    }
  }
}
