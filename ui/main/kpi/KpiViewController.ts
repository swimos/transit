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

import {Length} from "@swim/length";
import {Color} from "@swim/color";
import {HtmlView, HtmlViewController} from "@swim/view";
import {TextRunView} from "@swim/typeset";
import {PieView,
  // SliceView
} from "@swim/pie";

export abstract class KpiViewController extends HtmlViewController {
  /** @hidden */
  _updateTimer: number;

  /** @hidden */
  _kpiTitle?: HtmlView;

  /** @hidden */
  _subtitle?: HtmlView;

  /** @hidden */
  _pieView: PieView;

  /** @hidden */
  _pieTitle?: TextRunView;

  constructor() {
    super();
    this._updateTimer = 0;
  }

  abstract get primaryColor(): Color;

  abstract updateKpi(): void;

  get kpiTitle(): HtmlView | undefined {
    return this._kpiTitle;
  }

  get pieTitle(): TextRunView | undefined {
    return this._pieTitle;
  }

  get pieView(): PieView {
    return this._pieView;
  }

  didSetView(view: HtmlView): void {
    const primaryColor = this.primaryColor;

    view.display("flex")
        .flexDirection("column")
        .padding(8)
        .fontFamily("\"Open Sans\", sans-serif")
        .fontSize(12);

    const header = view.append("div")
        .display("flex")
        .justifyContent("space-between")
        .textTransform("uppercase")
        .color(primaryColor);

    const headerLeft = header.append("div");
    this._kpiTitle = headerLeft.append("span").display("block").text(" -- ");

    const headerRight = header.append("div");
    headerRight.append("span").text("Real-Time");

    const body = view.append("div").key("body").position("relative").flexGrow(1).width("100%");
    const bodyCanvas = body.append("canvas").key("canvas");

    this._pieView = new PieView()
        .key("pie")
        .innerRadius(Length.pct(34))
        .outerRadius(Length.pct(40))
        .cornerRadius(Length.pct(50))
        .tickRadius(Length.pct(45))
        .font("12px \"Open Sans\", sans-serif")
        .textColor(primaryColor);
    bodyCanvas.append(this._pieView);

    this._pieTitle = new TextRunView()
        .font("36px \"Open Sans\", sans-serif")
        .textColor(primaryColor);
    this._pieView.title(this._pieTitle);
  }

  viewDidMount(view: HtmlView): void {
    // force resize after flexbox layout
    requestAnimationFrame(function () { view.cascadeResize(); });

    // this._updateTimer = setInterval(this.updateKpi.bind(this), 1000) as any;
    this.updateKpi();
  }

  viewWillUnmount(view: HtmlView): void {
    clearInterval(this._updateTimer);
    this._updateTimer = 0;
  }
}
