package com.mapquest.navigation.sampleapp.activity;

import static com.mapquest.navigation.sampleapp.util.ColorUtil.getCongestionColor;
import static com.mapquest.navigation.sampleapp.util.ColorUtil.setOpacity;
import static com.mapquest.navigation.sampleapp.util.UiUtil.buildDownArrowMarkerOptions;
import static com.mapquest.navigation.sampleapp.util.UiUtil.toast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapLongClickListener;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapquest.navigation.ShapeSegmenter;
import com.mapquest.navigation.ShapeSegmenter.SpanPathPair;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.sampleapp.BuildConfig;
import com.mapquest.navigation.sampleapp.MQNavigationSampleApplication;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.searchahead.SearchAheadFragment;
import com.mapquest.navigation.sampleapp.searchahead.SearchAheadFragmentCallbacks;
import com.mapquest.navigation.sampleapp.searchahead.SearchAheadResult;
import com.mapquest.navigation.sampleapp.service.NavigationNotificationService;
import com.mapquest.navigation.sampleapp.searchahead.util.MQFontProviderUtil;
import com.mapquest.navigation.sampleapp.searchahead.util.AddressDisplayUtil;
import com.mapquest.navigation.sampleapp.util.LocationUtil;
import com.mapquest.navigation.internal.ShapeCalculator;
import com.mapquest.navigation.internal.ShapeCalculator.LineStringPoint;
import com.mapquest.navigation.internal.location.listener.LocationListener;
import com.mapquest.navigation.model.CongestionSpan;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.RouteOptions;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Location;
import com.mapquest.navigation.sampleapp.searchahead.util.MapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouteSelectionActivity extends AppCompatActivity implements SearchAheadFragmentCallbacks {

    private static final String TAG = LogUtil.generateLoggingTag(RouteSelectionActivity.class);

    private static final int REQUEST_LOCATION_PERMISSIONS = 0;

    private static final float DEFAULT_ZOOM_LEVEL = 13;
    private static final float CENTER_ON_USER_ZOOM_LEVEL = 16;

    private static final float DEFAULT_ROUTE_WIDTH = 5;
    private static final float SELECTED_ROUTE_WIDTH = 10;
    private static final float DEFAULT_ROUTE_OPACITY = 0.25f;
    private static final float SELECTED_ROUTE_OPACITY = 1.00f;

    private static final String SEARCH_AHEAD_FRAGMENT_TAG = "tag_search_ahead_fragment";

    SearchAheadFragment mSearchAheadFragment;

    @BindView(R.id.start)
    protected Button mStartButton;

    @BindView(R.id.search)
    protected Button mSearchButton;

    @BindView(R.id.route_name_text_view)
    protected TextView mRouteNameTextView;

    @BindView(R.id.map)
    protected MapView mMap;

    private MapboxMap mMapController;
    private Location mLastLocation;

    @BindView(R.id.gps_center_on_user_location_button)
    protected FloatingActionButton mGpsCenterOnUserLocationButton;

    @OnClick(R.id.gps_center_on_user_location_button)
    protected void centerOnUserLocation() {
        if (hasLocationPermissions()) {
            if (mLastLocation != null) {
                mMapController.moveCamera(CameraUpdateFactory.newLatLngZoom(toLatLng(mLastLocation), CENTER_ON_USER_ZOOM_LEVEL));
            } else {
                Toast.makeText(this, R.string.no_location, Toast.LENGTH_LONG).show();
            }
        }
    }

    private Marker mOriginMarker;
    private Marker mDestinationMarker;

    private RouteService mRouteService;
    private Map<Route, Set<Polyline>> mRoutePolylineSetsByRoute = new HashMap<>();
    private Route mSelectedRoute;

    private MQNavigationSampleApplication mApp;
    private LocationListener mFollowUserLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route_selection);
        ButterKnife.bind(this);

        MQFontProviderUtil.init(this);

        mApp = (MQNavigationSampleApplication) getApplication();
        mRouteService = new RouteService.Builder().build(getApplicationContext(), BuildConfig.API_KEY);

        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapController) {
                mMapController = mapController;
                initGpsButton();

                mapController.setOnMapClickListener(new RouteClickListener());

                requestLocationPermissions();
            }
        });

        mFollowUserLocationListener = new FollowUserLocationListener();
    }

    private void requestLocationPermissions() {
        Log.d(TAG, "requestLocationPermissions()");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Location Permissions Required");
            builder.setMessage("In Order to use the app you will need to accept the Location Permission request");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(RouteSelectionActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSIONS);
                }
            });

            builder.show();
        } else {
            // permission has not been granted yet, so request it directly
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            // check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mApp.initializeLocationProviderAdapter();
                acquireAndZoomToLocation(mMapController);
                enableButton(mSearchButton, true);
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Permissions Required");
                builder.setMessage("Without Location Permissions most of the app functionality will be disabled.  You will need to restart the app and accept the Location Permissions request");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                builder.show();
                enableButton(mSearchButton, false);
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return false;
        }
        return true;
    }

    private void enableButton(Button button, boolean enable) {
        button.setEnabled(enable);
        button.setTextColor(enable ? getResources().getColor(R.color.black) :
                getResources().getColor(R.color.disabled_grey));
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        mMap.onStart();

        clearMappedRoutes();
        removeDestinationMarker();

        mStartButton.setVisibility(View.GONE);
        mSearchButton.setVisibility(View.VISIBLE);
        mRouteNameTextView.setText("");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        mMap.onResume();
        mApp.getLocationProviderAdapter().addLocationListener(mFollowUserLocationListener);

        // Clear out NavigationNotificationService when coming back from NavigationActivity to destroy
        // all references to the existing NavigationManager and LocationProviderAdapter
        Intent mServiceIntent = new Intent(this, NavigationNotificationService.class);
        stopService(mServiceIntent);
    }

    @Override
    protected void onPause() {
        mMap.onPause();
        mApp.getLocationProviderAdapter().removeLocationListener(mFollowUserLocationListener);

        super.onPause();
    }

    @Override
    protected void onStop() {
        mMap.onStop();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mMap.onDestroy();

        super.onDestroy();
    }

    @OnClick(R.id.start)
    protected void startNavigationActivity() {
        mStartButton.setVisibility(View.GONE);
        mSearchButton.setVisibility(View.VISIBLE);
        mRouteNameTextView.setVisibility(View.GONE);

        NavigationActivity.start(this, mSelectedRoute);
    }

    @OnClick(R.id.search)
    protected void showSearchAheadFragment() {
        com.mapquest.android.commoncore.model.LatLng mapCenter = MapUtils.toMapQuestLatLng(mMapController.getCameraPosition().target);
        SearchAheadFragment fragment = SearchAheadFragment.newInstance(mapCenter);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, SEARCH_AHEAD_FRAGMENT_TAG)
                .addToBackStack(null).commit();
        mSearchAheadFragment = fragment;
    }

    private void acquireAndZoomToLocation(final MapboxMap mapController) {
        LocationUtil.acquireLocation(this, mApp.getLocationProviderAdapter(), new LocationProviderAdapter.LocationAcquisitionListener() {
            @Override
            public void onLocationAcquired(Location acquiredLocation) {
                markOrigin(acquiredLocation);

                mapController.moveCamera(CameraUpdateFactory.newLatLngZoom(toLatLng(acquiredLocation), DEFAULT_ZOOM_LEVEL));
                mapController.setOnMapLongClickListener(new OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng clickedLocation) {
                        markDestination(toCoordinate(clickedLocation));

                        mStartButton.setVisibility(View.GONE);
                        mRouteNameTextView.setVisibility(View.GONE);
                        mSelectedRoute = null;

                        retrieveRouteOptionsToLocation(clickedLocation);
                    }
                });
            }
        });
    }

    private void retrieveRouteOptionsToLocation(final LatLng location) {
        LocationUtil.acquireLocation(this, mApp.getLocationProviderAdapter(), new LocationProviderAdapter.LocationAcquisitionListener() {
            @Override
            public void onLocationAcquired(Location acquiredLocation) {

                RoutesResponseListener responseListener = new RoutesResponseListener() {
                    ProgressDialog mRoutingDialog;

                    @Override
                    public void onRequestMade() {
                        mRoutingDialog = displayProgressDialog("Routing", "Getting routes.");
                    }

                    @Override
                    public void onRoutesRetrieved(List<Route> routes) {
                        mRoutingDialog.dismiss();
                        toast(RouteSelectionActivity.this, routes.size() + " routes returned.");

                        mapRoutes(routes, null);
                        LatLng startLatLng = new LatLng(routes.get(0).getStartLocation().getLatitude(), routes.get(0).getStartLocation().getLongitude());
                        List<LatLng> latLngList = new ArrayList<LatLng>();
                        latLngList.add(startLatLng);
                        latLngList.add(location);
                        LatLngBounds latLngBounds = new LatLngBounds.Builder().include(latLngList).build();
                        mMapController.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 2, 100, 2));
                    }

                    @Override
                    public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {
                        mRoutingDialog.dismiss();

                        displayInformationalDialog("Error", "Sorry, couldn't get routes. :(\n\nCode: " + httpStatusCode
                                + "\nException: " + exception);
                    }
                };

                RouteOptions routeOptions = new RouteOptions.Builder()
                        .systemOfMeasurementForDisplayText(SystemOfMeasurement.UNITED_STATES_CUSTOMARY)
                        .build();

                mRouteService.requestRoutes(acquiredLocation, Collections.singletonList(toCoordinate(location)),
                        routeOptions, responseListener);
            }
        });
    }

    private void mapRoutes(Iterable<Route> routes, Route selectedRoute) {
        clearMappedRoutes();

        for (Route route : routes) {
            Set<Polyline> routeSegmentPolylines = route.equals(selectedRoute) ?
                    mapRoute(route, SELECTED_ROUTE_WIDTH, SELECTED_ROUTE_OPACITY) :
                    mapRoute(route, DEFAULT_ROUTE_WIDTH, DEFAULT_ROUTE_OPACITY);

            mRoutePolylineSetsByRoute.put(route, routeSegmentPolylines);
        }
    }

    private Set<Polyline> mapRoute(Route route, float lineWidth, float lineOpactity) {

        Set<Polyline> polylines = new HashSet<>();

        for (RouteLeg routeLeg: route.getLegs()) {
            polylines.addAll(mapLeg(routeLeg, lineWidth, lineOpactity));
        }

        return polylines;
    }

    private Set<Polyline> mapLeg(RouteLeg leg, float lineWidth, float lineOpactity) {

        List<SpanPathPair<CongestionSpan>> segments = new ShapeSegmenter.Builder().build()
                .segmentPath(leg.getShape(), leg.getTraffic().getConditions());

        Set<Polyline> polylines = new HashSet<>();
        for (SpanPathPair<CongestionSpan> segment : segments) {
            polylines.add(mMapController.addPolyline(buildSegmentPolylineOptions(
                    segment.getShapeCoordinates(),
                    setOpacity(getCongestionColor(segment.getSpan()), lineOpactity),
                    lineWidth)));
        }

        return polylines;
    }

    private PolylineOptions buildSegmentPolylineOptions(List<Coordinate> path, int color, float width) {
        PolylineOptions options = new PolylineOptions()
                .width(width)
                .color(color);

        for (Coordinate coordinate : path) {
            options.add(toLatLng(coordinate));
        }

        return options;
    }

    private void clearMappedRoutes() {
        for (Set<Polyline> polylines : mRoutePolylineSetsByRoute.values()) {
            for (Polyline polyline : polylines) {
                mMapController.removePolyline(polyline);
            }
        }

        mRoutePolylineSetsByRoute.clear();
    }

    private ProgressDialog displayProgressDialog(String title, String message) {
        ProgressDialog dialog = new ProgressDialog(this);

        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }

    private AlertDialog displayInformationalDialog(String title, String message) {
        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private static Coordinate toCoordinate(LatLng location) {
        return new Coordinate(location.getLatitude(), location.getLongitude());
    }

    private void markOrigin(Coordinate location) {
        if (mMapController != null) {
            if (mOriginMarker != null) {
                mMapController.removeMarker(mOriginMarker);
            }

            mOriginMarker = markLocation(toLatLng(location), R.color.marker_greeen);
        }
    }

    private void markDestination(Coordinate location) {
        removeDestinationMarker();

        mDestinationMarker = markLocation(toLatLng(location), R.color.marker_red);
    }

    private Marker markLocation(LatLng latLng, @ColorRes int fillColorResourceId) {
        return mMapController.addMarker(buildDownArrowMarkerOptions(this, fillColorResourceId)
                .position(latLng));
    }

    private void removeDestinationMarker() {
        if(mDestinationMarker != null) {
            mMapController.removeMarker(mDestinationMarker);
            mDestinationMarker = null;
        }
    }

    private static LatLng toLatLng(Coordinate coordinate) {
        return new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
    }

    @Override
    public void onSelectResult(SearchAheadResult result) {
        if (result.getAddress().getDisplayGeoPoint() != null) {
            Log.d(TAG, "onSelectResult(): good address");
            mSearchAheadFragment.setSearchFieldNoQuery(AddressDisplayUtil.forResources(getResources()).getDisplayString(result));
            getSupportFragmentManager().beginTransaction().remove(mSearchAheadFragment).commit();
            mMapController.getOnMapLongClickListener().onMapLongClick(MapUtils.toMapBoxLatLng(result.getAddress().getDisplayGeoPoint()));
        } else {
            Log.e(TAG, "onSelectResult(): bad address, lat long not available");
            toast(RouteSelectionActivity.this, " Coordinates for searched location not available!");
        }
    }

    @Override
    public void onCancel() {
        Log.d(TAG, "onCancel()");
        getSupportFragmentManager().beginTransaction().remove(mSearchAheadFragment).commit();
    }

    private class RouteClickListener implements OnMapClickListener {
        @Override
        public void onMapClick(@NonNull LatLng latLng) {
            List<Route> routes = new ArrayList<>(mRoutePolylineSetsByRoute.keySet());
            clearMappedRoutes();

            mSelectedRoute = findNearestRoute(routes, toCoordinate(latLng));
            if(mSelectedRoute != null) {
                // Move to the end, so the selected route gets drawn over the rest
                if(routes.remove(mSelectedRoute)) {
                    routes.add(mSelectedRoute);

                    mapRoutes(routes, mSelectedRoute);
                }

                mStartButton.setVisibility(View.VISIBLE);
                mSearchButton.setVisibility(View.GONE);

                mRouteNameTextView.setVisibility(View.VISIBLE);
                mRouteNameTextView.setText(mSelectedRoute.getName() != null ? mSelectedRoute.getName() : "");
            }
        }
    }

    private static Route findNearestRoute(Iterable<Route> routes, Coordinate point) {
        Double nearestDistance = null;
        Route nearestRoute = null;

        ShapeSegmenter shapeSegmenter = new ShapeSegmenter.Builder().build();

        for (Route route : routes) {

            LineStringPoint result = null;

            for (RouteLeg routeLeg: route.getLegs()) {
                LineStringPoint pointForLeg = ((ShapeCalculator) shapeSegmenter)
                        .findClosestPoint(point, routeLeg.getShape().getCoordinates());
                if (result == null || pointForLeg.getArcLengthFromTestPoint() < result.getArcLengthFromTestPoint()) {
                    result = pointForLeg;
                }
            }

            if (nearestDistance == null || result.getArcLengthFromTestPoint() < nearestDistance) {
                nearestDistance = result.getArcLengthFromTestPoint();
                nearestRoute = route;
            }
        }

        return nearestRoute;
    }

    /** Make gps button visible and set up long click listener */
    private void initGpsButton() {
        mGpsCenterOnUserLocationButton.setVisibility(View.VISIBLE);
        mGpsCenterOnUserLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasLocationPermissions()) {
                    // re-acquire current location and zoom/center map to that location
                    acquireAndZoomToLocation(mMapController);
                }
            }
        });
    }

    private class FollowUserLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            markOrigin(location);
        }
    }
}
