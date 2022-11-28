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

import {NodeRef} from "@swim/client";
import {Color} from "@swim/color";
import {PopoverView, PopoverViewController} from "@swim/view";
import {AgencyInfo} from "./AgencyModel";

export class AgencyPopoverViewController extends PopoverViewController {
  /** @hodden */
  _info: AgencyInfo;
  /** @hidden */
  _nodeRef: NodeRef;

  constructor(info: AgencyInfo, nodeRef: NodeRef) {
    super();
    this._info = info;
    this._nodeRef = nodeRef;
  }

  didSetView(view: PopoverView): void {
    view.width(240)
        .height(360)
        .borderRadius(5)
        .padding(10)
        .backgroundColor(Color.parse("#071013").alpha(0.9))
        .backdropFilter("blur(2px)");

    const agency = this._info;

    const container = view.append("div").color("#ffffff");

    container.append("span").key("name").text("" + agency.id);

    // TODO: layout popover
  }
}
