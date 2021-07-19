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

package swim.transit;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import swim.api.ref.SwimRef;
import swim.codec.Utf8;
import swim.structure.Form;
import swim.structure.Item;
import swim.structure.Value;
import swim.transit.model.Agency;
import swim.transit.model.Route;
import swim.transit.model.Routes;
import swim.transit.model.Vehicle;
import swim.transit.model.Vehicles;
import swim.xml.Xml;

public class NextBusHttpAPI {

  private NextBusHttpAPI() { }

  public static void sendRoutes(List<Agency> agencies, SwimRef swim) {
    for (Agency agency: agencies) {
      sendRoutes(agency, swim);
    }
  }

  public static void sendVehicleInfo(String pollUrl, Agency ag, SwimRef swim) {
    final Vehicles vehicles = getVehicleLocations(pollUrl, ag);
    if (vehicles != null && vehicles.getVehicles().size() > 0) {
      final Value value = Form.forClass(Vehicles.class).mold(vehicles).toValue();
      swim.command(ag.getUri(), "addVehicles", value);
    }
  }

  private static void sendRoutes(Agency agency, SwimRef swim) {
    final Routes routes = getRoutes(agency);
    if (routes != null && !routes.getRoutes().isEmpty()) {
      final Value value = Form.forClass(Routes.class).mold(routes).toValue();
      swim.command(agency.getUri(), "addRoutes", value);
    }
  }

  private static Routes getRoutes(Agency ag) {
    try {
      final URL url = new URL(String.format(
          "https://retro.umoiq.com/service/publicXMLFeed?command=routeList&a=%s", ag.getId()));
      final Value allRoutes = parse(url);
      if (!allRoutes.isDefined()) {
        return null;
      }
      final Iterator<Item> it = allRoutes.iterator();
      final Routes routes = new Routes();
      while (it.hasNext()) {
        final Item item = it.next();
        final Value header = item.getAttr("route");
        if (header.isDefined()) {
          final Route route = new Route().withTag(header.get("tag").stringValue()).withTitle(header.get("title").stringValue());
          routes.add(route);
        }
      }
      return routes;
    } catch (Exception e) {
    }
    return null;
  }

  public static Vehicles getVehicleLocations(String pollUrl, Agency ag) {
    try {
      final URL url = new URL(pollUrl);
      final Value vehicleLocs = parse(url);
      if (!vehicleLocs.isDefined()) {
        return null;
      }

      final Iterator<Item> it = vehicleLocs.iterator();
      final Vehicles vehicles = new Vehicles();
      while (it.hasNext()) {
        final Item item = it.next();
        final Value header = item.getAttr("vehicle");
        if (header.isDefined()) {
          final String id = header.get("id").stringValue().trim();
          final String routeTag = header.get("routeTag").stringValue();
          final float latitude = header.get("lat").floatValue(0.0f);
          final float longitude = header.get("lon").floatValue(0.0f);
          final int speed = header.get("speedKmHr").intValue(0);
          final int secsSinceReport = header.get("secsSinceReport").intValue(0);
          final String dir = header.get("dirTag").stringValue("");
          final String dirId;
          if (!dir.equals("")) {
            dirId = dir.contains("_0") ? "outbound" : "inbound";
          } else {
            dirId = "outbound";
          }

          final int headingInt = header.get("heading").intValue(0);
          String heading = "";
          if (headingInt < 23 || headingInt >= 338) {
            heading = "E";
          } else if (23 <= headingInt && headingInt < 68) {
            heading = "NE";
          } else if (68 <= headingInt && headingInt < 113) {
            heading = "N";
          } else if (113 <= headingInt && headingInt < 158) {
            heading = "NW";
          } else if (158 <= headingInt && headingInt < 203) {
            heading = "W";
          } else if (203 <= headingInt && headingInt < 248) {
            heading = "SW";
          } else if (248 <= headingInt && headingInt < 293) {
            heading = "S";
          } else if (293 <= headingInt && headingInt < 338) {
            heading = "SE";
          }
          final String uri = "/vehicle/" + ag.getCountry() + "/" + ag.getState() + "/" + ag.getId() + "/" + parseUri(id);
          final Vehicle vehicle = new Vehicle().withId(id).withUri(uri).withDirId(dirId).withIndex(ag.getIndex())
              .withLatitude(latitude).withLongitude(longitude).withRouteTag(routeTag).withSecsSinceReport(secsSinceReport)
              .withSpeed(speed).withHeading(heading);
          vehicles.add(vehicle);
        }
      }
      return vehicles;
    } catch (Exception e) {
    }
    return null;
  }

  private static String parseUri(String uri) {
    try {
      return java.net.URLEncoder.encode(uri, "UTF-8").toString();
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }

  private static Value parse(URL url) {
    final HttpURLConnection urlConnection;
    try {
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
      final InputStream stream = new GZIPInputStream(urlConnection.getInputStream());
      final Value configValue = Utf8.read(Xml.structureParser().documentParser(), stream);
      return configValue;
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return Value.absent();
  }

}
