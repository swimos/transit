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

package swim.transit.agent;

import java.util.Collection;
import java.util.Map;
import swim.api.SwimLane;
import swim.api.SwimTransient;
import swim.api.agent.AbstractAgent;
import swim.api.lane.CommandLane;
import swim.api.lane.MapLane;
import swim.api.lane.ValueLane;
import swim.concurrent.AbstractTask;
import swim.concurrent.TaskRef;
import swim.concurrent.TimerRef;
import swim.structure.Value;
import swim.transit.NextBusHttpAPI;
import swim.transit.model.Agency;
import swim.transit.model.BoundingBox;
import swim.transit.model.Route;
import swim.transit.model.Routes;
import swim.transit.model.Vehicle;
import swim.transit.model.Vehicles;

public class AgencyAgent extends AbstractAgent {
  @SwimTransient
  @SwimLane("vehicles")
  public MapLane<String, Vehicle> vehicles;

  @SwimLane("count")
  public ValueLane<Integer> vehiclesCount;

  @SwimLane("speed")
  public ValueLane<Float> vehiclesSpeed;

  @SwimLane("addVehicles")
  public CommandLane<Vehicles> addVehicles = this.<Vehicles>commandLane().onCommand(this::onVehicles);

  @SwimLane("boundingBox")
  public ValueLane<BoundingBox> boundingBox;

  private void onVehicles(Vehicles newVehicles) {
    if (newVehicles == null || newVehicles.getVehicles().size() == 0) {
      return;
    }
    updateVehicles(newVehicles.getVehicles());
    int speedSum = 0;
    float minLat = Integer.MAX_VALUE, minLng = Integer.MAX_VALUE, maxLat = Integer.MIN_VALUE, maxLng = Integer.MIN_VALUE;

    for (Vehicle v : newVehicles.getVehicles().values()) {
      final String vehicleUri = v.getUri();
      if (vehicleUri != null && !vehicleUri.equals("")) {
        context.command(vehicleUri, "addVehicle", v.toValue());
        addVehicle(vehicleUri, v);
        speedSum += v.getSpeed();
        if (v.getLatitude() < minLat) {
          minLat = v.getLatitude();
        }
        if (v.getLatitude() > maxLat) {
          maxLat = v.getLatitude();
        }
        if (v.getLongitude() < minLng) {
          minLng = v.getLongitude();
        }
        if (v.getLongitude() > maxLng) {
          maxLng = v.getLongitude();
        }
      }
    }

    boundingBox.set(new BoundingBox(minLng, minLat, maxLng, maxLat));
    vehiclesCount.set(this.vehicles.size());
    if (vehiclesCount.get() > 0) {
      vehiclesSpeed.set(((float) speedSum) / vehiclesCount.get());
    }
  }

  private void updateVehicles(Map<String, Vehicle> newVehicles) {
    final Collection<Vehicle> currentVehicles = this.vehicles.values();
    for (Vehicle vehicle : currentVehicles) {
      if (!newVehicles.containsKey(vehicle.getUri())) {
        vehicles.remove(vehicle.getUri());
      }
    }
  }

  private void addVehicle(String vehicleUri, Vehicle v) {
    final Route r = routes.get(v.getRouteTag());
    if (r != null) {
      this.vehicles.put(vehicleUri, v.withAgency(getProp("id").stringValue("")).withRouteTitle(r.getTitle()));
    }
  }

  @SwimLane("addInfo")
  public CommandLane<Agency> addInfo = this.<Agency>commandLane().onCommand(this::onInfo);

  @SwimLane("info")
  public ValueLane<Agency> info = this.<Agency>valueLane()
    .didSet((n, o) -> {
      abortPoll();
      startPoll(n);
    });

  private void onInfo(Agency agency) {
    final Value agencyValue = agency.toValue().unflattened().slot("agencyUri", this.nodeUri().toString());
    context.command("/state/" + agency.getCountry() + "/" + agency.getState(), "addAgency", agencyValue);
    info.set(agency);
  }

  @SwimLane("addRoutes")
  public CommandLane<Routes> addRoutes = this.<Routes>commandLane().onCommand(this::onRoutes);

  @SwimLane("routes")
  public MapLane<String, Route> routes;

  private void onRoutes(Routes r) {
    for (Route route : r.getRoutes()) {
      routes.put(route.getTag(), route);
    }
  }

  private TaskRef pollVehicleInfo;

  private TimerRef timer;

  private void startPoll(final Agency ag) {
    abortPoll();

    // Define task
    this.pollVehicleInfo = asyncStage().task(new AbstractTask() {

      final Agency agency = ag;
      final String url = String.format("https://retro.umoiq.com/service/publicXMLFeed?command=vehicleLocations&a=%s&t=0",
        ag.getId());

      @Override
      public void runTask() {
        NextBusHttpAPI.sendVehicleInfo(this.url, this.agency, AgencyAgent.this.context);
      }

      @Override
      public boolean taskWillBlock() {
        return true;
      }
    });

    // Define timer to periodically reschedule task
    if (this.pollVehicleInfo != null) {
      this.timer = setTimer(1000, () -> {
        this.pollVehicleInfo.cue();
        this.timer.reschedule(POLL_INTERVAL);
      });
    }
  }

  private void abortPoll() {
    if (this.pollVehicleInfo != null) {
      this.pollVehicleInfo.cancel();
      this.pollVehicleInfo = null;
    }
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }
  }

  @Override
  public void didStart() {
    vehicles.clear();
    vehiclesSpeed.set((float) 0);
    vehiclesCount.set(0);
    System.out.println("Started Agent: " + nodeUri().toString());
  }

  @Override
  public void willStop() {
    abortPoll();
    super.willStop();
  }

  private static final long POLL_INTERVAL = 10000L;
}
