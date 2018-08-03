package com.mapquest.navigation.sampleapp.routesettings;

import java.util.List;

public interface RouteStopsChangedListener {
    void routeStopAdded(RouteStop routeStopAdded, List<RouteStop> allStops);
    void routeStopRemoved(RouteStop routeStopRemoved, List<RouteStop> allStops);
}
