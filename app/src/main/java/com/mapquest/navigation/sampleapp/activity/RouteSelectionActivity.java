package com.mapquest.navigation.sampleapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapquest.mapping.maps.RoutePolylinePresenter;
import com.mapquest.navigation.dataclient.RouteService;
import com.mapquest.navigation.dataclient.listener.RoutesResponseListener;
import com.mapquest.navigation.internal.ShapeCalculator;
import com.mapquest.navigation.internal.dataclient.NavigationRouteServiceFactory;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.location.LocationListener;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.CongestionSpan;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.RouteOptions;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Destination;
import com.mapquest.navigation.sampleapp.BuildConfig;
import com.mapquest.navigation.sampleapp.MQNavigationSampleApplication;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.location.CurrentLocationProvider;
import com.mapquest.navigation.sampleapp.searchahead.SearchAheadFragment;
import com.mapquest.navigation.sampleapp.searchahead.SearchBarView;
import com.mapquest.navigation.sampleapp.service.NavigationNotificationService;
import com.mapquest.navigation.sampleapp.util.LocationUtil;
import com.mapquest.navigation.util.ShapeSegmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mapquest.navigation.sampleapp.util.ColorUtil.getCongestionColor;
import static com.mapquest.navigation.sampleapp.util.ColorUtil.setOpacity;
import static com.mapquest.navigation.sampleapp.util.MapUtils.toMapQuestLatLng;
import static com.mapquest.navigation.sampleapp.util.UiUtil.buildDownArrowMarkerOptions;
import static com.mapquest.navigation.sampleapp.util.UiUtil.toast;

public class RouteSelectionActivity extends AppCompatActivity
        implements CurrentLocationProvider, SearchAheadFragment.OnSearchResultSelectedListener {

    private static final String TAG = LogUtil.generateLoggingTag(RouteSelectionActivity.class);

    private static final int REQUEST_LOCATION_PERMISSIONS = 0;

    private static final float DEFAULT_ZOOM_LEVEL = 13;
    private static final float CENTER_ON_USER_ZOOM_LEVEL = 16;

    private static final float DEFAULT_ROUTE_WIDTH = 5;
    private static final float SELECTED_ROUTE_WIDTH = 10;
    private static final float DEFAULT_ROUTE_OPACITY = 0.25f;
    private static final float SELECTED_ROUTE_OPACITY = 1.00f;

    private static final String SEARCH_AHEAD_FRAGMENT_TAG = "tag_search_ahead_fragment";

    private static final String SHARED_PREFERENCE_NAME = "com.mapquest.navigation.sampleapp.activity.RouteSelectionActivity";
    private static final String USER_TRACKING_CONSENT_KEY = "user_tracking_consent";

    @BindView(R.id.start)
    protected Button mStartButton;

    @BindView(R.id.retrieve_routes)
    protected Button mRetrieveRoutesButton;

    @BindView(R.id.clear_routes)
    protected Button mClearRoutesButton;

    @BindView(R.id.route_name_text_view)
    protected TextView mRouteNameTextView;

    @BindView(R.id.map)
    protected MapView mMap;

    private MapboxMap mMapController;
    private RoutePolylinePresenter mRoutePolylinePresenter;
    private MapboxMap.OnMapLongClickListener mMapLongClickListener;
    private ProgressDialog mRoutingDialog;

    private com.mapquest.navigation.model.location.Location mLastLocation;
    private LocationPermissionsResultListener locationPermissionsResultListener;

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

    private Coordinate mStartingCoordinate;
    private List<Destination> mDestinationLocations = new ArrayList<>();

    private Marker mOriginMarker;
    private List<Marker> mDestinationMarkers = new ArrayList<>();

    private RouteService mRouteService;
    private Map<Route, List<PolylineOptions>> mRoutePolylineOptionsListByRoute = new HashMap<>();
    private Route mSelectedRoute;

    private MQNavigationSampleApplication mApp;
    private LocationListener mFollowUserLocationListener;

    private Float mMapExtentPaddingTop = null;
    private Float mMapExtentPaddingBottom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route_selection);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mApp = (MQNavigationSampleApplication) getApplication();
        mRouteService = new RouteService.Builder().build(getApplicationContext(), BuildConfig.API_KEY);

        // setup search-bar placeholder view; will display search-ahead fragment when clicked
        SearchBarView searchBarView = toolbar.findViewById(R.id.fake_search_bar_view);
        searchBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchAheadFragment searchAheadFragment = SearchAheadFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                            .add(android.R.id.content, searchAheadFragment, SEARCH_AHEAD_FRAGMENT_TAG)
                            .addToBackStack(null)
                            .commit();
            }
        });

        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapController) {
                mMapController = mapController;
                mRoutePolylinePresenter = new RoutePolylinePresenter(mMap, mMapController);
                initGpsButton();

                // note: regular click on map will select a *route* to navigate
                mapController.setOnMapClickListener(new RouteClickListener());

                // long-clicks on map are used to define the start and end-points for a route to request
                mMapLongClickListener = new MapboxMap.OnMapLongClickListener() {
                    // long-pressed location is now the next "waypoint" destination
                    @Override
                    public void onMapLongClick(@NonNull LatLng clickedLocation) {
                        Coordinate destinationCoordinate = toCoordinate(clickedLocation);
                        addDestinationToRoute(destinationCoordinate, null);
                    }
                };

                requestLocationPermissions(new LocationPermissionsResultListener() {
                    @Override
                    public void onRequestLocationPermissionsResult(boolean locationPermissionsWereGranted) {
                        if (locationPermissionsWereGranted) {
                            // OK, now that we have location permissions, init our GPS location-provider, and zoom the map-view
                            mApp.getLocationProviderAdapter().initialize();
                            acquireCurrentLocationAndZoom(mMapController);

                        } else {
                            // permission "denied"... so we cannot acquire the current location; explain why perms are needed
                            final AlertDialog.Builder builder = new AlertDialog.Builder(RouteSelectionActivity.this);
                            builder.setTitle("Location Permissions Required");
                            builder.setMessage(
                                    "Without Location Permissions most of the app functionality will be disabled.  " +
                                            "You will need to restart the app and accept the Location Permissions request");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            builder.show();
                        }
                    }
                });
            }
        });
        mFollowUserLocationListener = new FollowUserLocationListener();

        clearMappedRoutes();
        for (Marker marker : mDestinationMarkers) {
            removeDestinationMarker(marker);
        }

        mDestinationLocations.clear();

        enableButton(mClearRoutesButton, false);
        enableButton(mRetrieveRoutesButton, false);
        enableButton(mStartButton, false);
    }

    private void requestLocationPermissions(LocationPermissionsResultListener permissionsResultListener) {
        this.locationPermissionsResultListener = permissionsResultListener;

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
                            new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                            REQUEST_LOCATION_PERMISSIONS);
                }
            });
            builder.show();
        } else {
            // permission has not been granted yet, so request it directly
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_LOCATION_PERMISSIONS);
        }
    }

    // callback received when permission request has been acted on by user; so call our listener with the result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            // check that the requested permission has been granted
            boolean locationPermissionsWereGranted = (grantResults.length == 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            locationPermissionsResultListener.onRequestLocationPermissionsResult(locationPermissionsWereGranted);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected interface LocationPermissionsResultListener {
        void onRequestLocationPermissionsResult(boolean locationPermissionsWereGranted);
    }

    protected boolean hasLocationPermissions() {
        return(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    @OnClick(R.id.retrieve_routes)
    protected void retrieveRoutes() {
        mRouteNameTextView.setVisibility(View.GONE);
        acquireCurrentLocationAndRetrieveRoutesToDestinations(mDestinationLocations);
    }

    @OnClick(R.id.clear_routes)
    protected void clearRoutes() {
        clearMappedRoutes();
        for (Marker marker : new ArrayList<>(mDestinationMarkers)) {
            removeDestinationMarker(marker);
        }

        mDestinationLocations.clear();

        mRouteNameTextView.setVisibility(View.GONE);
        enableButton(mClearRoutesButton, false);
        enableButton(mRetrieveRoutesButton, false);
        enableButton(mStartButton, false);

        Toast.makeText(getApplicationContext(), "All points cleared. Starting location is current location.\n\n" +
                "Long-press on the map to add a new destination location...", Toast.LENGTH_LONG).show();
    }

    private void enableButton(Button button, boolean enable) {
        button.setEnabled(enable);
        button.setTextColor(enable ? getResources().getColor(R.color.black) :
                getResources().getColor(R.color.disabled_grey));
    }

    @Override
    public void onSearchResultSelected(String displayName, Coordinate coordinate, String mqId) {
        // add selected search-result as (another) destination to route
        addDestinationToRoute(coordinate, mqId);
    }

    private void addDestinationToRoute(Coordinate destinationCoordinate, String mqId) {

        mDestinationMarkers.add(markDestination(destinationCoordinate, R.color.marker_blue));

        // get bounding-rect of origin point and all destinations; adjust map-view accordingly to show all
        LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
        latLngBoundsBuilder.include(mOriginMarker.getPosition());
        for (Marker marker: mDestinationMarkers) {
            latLngBoundsBuilder.include(marker.getPosition());
        }

        mMapController.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(),
                100,
                (int) getMapExtentPaddingTop(),
                100,
                (int) getMapExtentPaddingBottom()));

        mSelectedRoute = null;

        Log.d(TAG, "addDestinationToRoute: adding new destination location with mqid " + mqId);
        mDestinationLocations.add(new Destination(destinationCoordinate, mqId));

        enableButton(mClearRoutesButton, true);
        enableButton(mRetrieveRoutesButton, true);
        enableButton(mStartButton, false);
    }

    // Lazy load map top padding
    private float getMapExtentPaddingTop() {
        if (mMapExtentPaddingTop == null) {
            int searchBarViewHeightWithPadding = 85 + (2*16); // Based off search bar view height, padding, & marker height
            mMapExtentPaddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    searchBarViewHeightWithPadding, getResources().getDisplayMetrics());
        }
        return mMapExtentPaddingTop;
    }

    // Lazy load map bottom padding
    private float getMapExtentPaddingBottom() {
        if (mMapExtentPaddingBottom == null) {
            int routeButtons = 50 + (2*16); // per route buttons at bottom of map's height & padding
            mMapExtentPaddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    routeButtons, getResources().getDisplayMetrics());
        }
        return mMapExtentPaddingBottom;
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

         mMap.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        mMap.onResume();
        mApp.getLocationProviderAdapter().addLocationListener(mFollowUserLocationListener);

        enableButton(mStartButton, (mSelectedRoute != null)); // enable "start nav" if there's (still) a selected route

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
        mRouteNameTextView.setVisibility(View.GONE);

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        if (sharedPreferences.contains(USER_TRACKING_CONSENT_KEY)) {
            boolean userGrantedLocationTrackingConsent = sharedPreferences.getBoolean(USER_TRACKING_CONSENT_KEY, false);

            Intent navigationActivityIntent = NavigationActivity
                    .buildNavigationActivityIntent(this, mSelectedRoute, userGrantedLocationTrackingConsent);

            startActivity(navigationActivityIntent);

        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.user_tracking_consent_dialog_title)
                    .setMessage(R.string.user_tracking_consent_dialog_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.user_tracking_consent_dialog_positive_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sharedPreferences.edit()
                                            .putBoolean(USER_TRACKING_CONSENT_KEY, true)
                                            .apply();

                                    Intent navigationActivityIntent = NavigationActivity
                                            .buildNavigationActivityIntent(RouteSelectionActivity.this,
                                                    mSelectedRoute, true);

                                    startActivity(navigationActivityIntent);

                                }
                            })
                    .setNegativeButton(R.string.user_tracking_consent_dialog_negative_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sharedPreferences.edit()
                                            .putBoolean(USER_TRACKING_CONSENT_KEY, false)
                                            .apply();

                                    Intent navigationActivityIntent = NavigationActivity
                                            .buildNavigationActivityIntent(RouteSelectionActivity.this,
                                                    mSelectedRoute, false);

                                    startActivity(navigationActivityIntent);
                                }
                            })
                    .show();
        }
    }

    private void acquireCurrentLocationAndZoom(final MapboxMap mapController) {
        LocationProviderAdapter locationProviderAdapter = mApp.getLocationProviderAdapter();
        LocationUtil.acquireLocation(this, locationProviderAdapter,
                new LocationProviderAdapter.LocationAcquisitionListener() {
                    @Override
                    public void onLocationAcquired(final com.mapquest.navigation.model.location.Location acquiredLocation) {
                        mStartingCoordinate = acquiredLocation;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mapController.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(toLatLng(acquiredLocation), DEFAULT_ZOOM_LEVEL));
                                mapController.setOnMapLongClickListener(mMapLongClickListener);
                                markOrigin(acquiredLocation);

                                Toast.makeText(getApplicationContext(), "Starting point is your Current Location.\n\n" +
                                        "Long-press on the map to add a destination location...", Toast.LENGTH_LONG).show();
                            }
                        }, 100);
                    }
                });
    }

    private void acquireCurrentLocationAndRetrieveRoutesToDestinations(final List<Destination> destinationLocations) {
        LocationProviderAdapter locationProviderAdapter = mApp.getLocationProviderAdapter();
        LocationUtil.acquireLocation(this, locationProviderAdapter,
                new LocationProviderAdapter.LocationAcquisitionListener() {
                    @Override
                    public void onLocationAcquired(com.mapquest.navigation.model.location.Location acquiredLocation) {
                        mStartingCoordinate = acquiredLocation;
                        retrieveRouteFromStartingLocationToDestinations(acquiredLocation, destinationLocations);
                    }
                });
    }

    private void retrieveRouteFromStartingLocationToDestinations(final Coordinate startingCoordinate, final List<Destination> destinationLocations) {
        RoutesResponseListener responseListener = new RoutesResponseListener() {
            @Override
            public void onRoutesRetrieved(@NonNull List<com.mapquest.navigation.model.Route> routes) {
                if(mRoutingDialog != null) {
                    mRoutingDialog.dismiss();
                }
                mapRoutes(routes, null);
                if(routes.size() > 1) {
                    toast(RouteSelectionActivity.this, routes.size() + " routes returned.\n\nChoose one to navigate by tapping on it.");
                } else {
                    // if only one route returned, auto-select it
                    setSelectedRoute(routes.get(0));
                }
            }

            @Override
            public void onRequestMade() {
                mRoutingDialog = displayProgressDialog("Routing", "Getting routes...");
            }

            @Override
            public void onRequestFailed(@Nullable Integer httpStatusCode,
                                        @Nullable IOException exception) {
                Log.d(TAG, "RoutesResponseListener.onRequestFailed: statusCode: " + httpStatusCode + "; exception: " + exception);

                mRoutingDialog.dismiss();
                displayInformationalDialog("Error",
                        "Sorry, couldn't get routes. :(\n\nCode: " + httpStatusCode
                                + "\nException: " + exception);
            }
        };

        mRouteService = NavigationRouteServiceFactory.getNavigationRouteService(getApplicationContext(), BuildConfig.API_KEY);
        RouteOptions routeOptions = new RouteOptions.Builder()
                .systemOfMeasurementForDisplayText(SystemOfMeasurement.UNITED_STATES_CUSTOMARY) // or specify METRIC
                .language("en_US") // NOTE: alternately, specify "es_US" for Spanish in the US
                .build();

        try {
            mRouteService.requestRoutes(startingCoordinate, destinationLocations, routeOptions, responseListener);
        } catch (IllegalArgumentException e) {
            toast(RouteSelectionActivity.this, e.getLocalizedMessage());
        }
    }

    private void mapRoutes(Iterable<Route> routes, Route selectedRoute) {
        clearMappedRoutes();
        for (Route route : routes) {
            List<PolylineOptions> routeSegmentPolylines = route.equals(selectedRoute) ?
                    mapRoute(route, SELECTED_ROUTE_WIDTH, SELECTED_ROUTE_OPACITY) :
                    mapRoute(route, DEFAULT_ROUTE_WIDTH, DEFAULT_ROUTE_OPACITY);

            mRoutePolylineOptionsListByRoute.put(route, routeSegmentPolylines);
        }
    }

    private List<PolylineOptions> mapRoute(Route route, float lineWidth, float lineOpacity) {

        List<PolylineOptions> polylinesOptionsList = new ArrayList<>();
        for (RouteLeg routeLeg: route.getLegs()) {
            polylinesOptionsList.addAll(mapLeg(routeLeg, lineWidth, lineOpacity));
        }
        return polylinesOptionsList;
    }

    private List<PolylineOptions> mapLeg(RouteLeg leg, float lineWidth, float lineOpacity) {

        List<ShapeSegmenter.SpanPathPair<CongestionSpan>> segments = new ShapeSegmenter.Builder().build()
                .segmentPath(leg.getShape(), leg.getTraffic().getConditions());

        List<PolylineOptions> polylinesList = new ArrayList<>();
        for (ShapeSegmenter.SpanPathPair<CongestionSpan> segment : segments) {
            PolylineOptions polylineOptions = buildSegmentPolylineOptions(
                    segment.getShapeCoordinates(),
                    setOpacity(getCongestionColor(segment.getSpan()), lineOpacity),
                    lineWidth);
            polylinesList.add(polylineOptions);
            mRoutePolylinePresenter.addPolyline(polylineOptions);
        }
        return polylinesList;
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
        for (List<PolylineOptions> polylineOptionsList : mRoutePolylineOptionsListByRoute.values()) {
            for (PolylineOptions polylineOptions : polylineOptionsList) {
                mRoutePolylinePresenter.removePolyline(polylineOptions);
            }
        }

        mRoutePolylineOptionsListByRoute.clear();
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
            mOriginMarker = markLocation(toLatLng(location), R.color.marker_green);
        }
    }

    private Marker markDestination(Coordinate location, int color) {
        return(markLocation(toLatLng(location), color));
    }

    private Marker markLocation(LatLng latLng, @ColorRes int fillColorResourceId) {
        return mMapController.addMarker(buildDownArrowMarkerOptions(this, fillColorResourceId)
                .position(latLng));
    }

    private void removeDestinationMarker(Marker marker) {
        if (marker != null) {
            mMapController.removeMarker(marker);
            mDestinationMarkers.remove(marker);
        }
    }

    private static LatLng toLatLng(Coordinate coordinate) {
        return new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
    }

    @Nullable
    @Override
    public com.mapquest.android.commoncore.model.LatLng getCurrentLocation() {
        return toMapQuestLatLng(mMapController.getCameraPosition().target);
    }

    private class RouteClickListener implements MapboxMap.OnMapClickListener {
        @Override
        public void onMapClick(@NonNull LatLng latLng) {
            List<Route> drawnRoutes = new ArrayList<>(mRoutePolylineOptionsListByRoute.keySet());
            setSelectedRoute(findNearestRoute(drawnRoutes, toCoordinate(latLng)));

//            List<Route> routes = new ArrayList<>(mRoutePolylineOptionsListByRoute.keySet());
//            clearMappedRoutes();
//
//            mSelectedRoute = findNearestRoute(routes, toCoordinate(latLng));
//            if(mSelectedRoute != null) {
//                // Move to the end, so the selected route gets drawn over the rest
//                if(routes.remove(mSelectedRoute)) {
//                    routes.add(mSelectedRoute);
//
//                    mapRoutes(routes, mSelectedRoute);
//                }
//                mRouteNameTextView.setText(mSelectedRoute.getName() != null ? mSelectedRoute.getName() : "");
//                mRouteNameTextView.setVisibility(View.VISIBLE);
//
//                enableButton(mStartButton, true);
//            }
        }
    }

    private void setSelectedRoute(Route selectedRoute) {
        if(selectedRoute != null) {
            mSelectedRoute = selectedRoute;

            // re-draw routes; highlighting the selected-route
            List<Route> drawnRoutes = new ArrayList<>(mRoutePolylineOptionsListByRoute.keySet());
            clearMappedRoutes();

            // note: move selected-route to the end, so that it gets drawn on top of the rest
            if(drawnRoutes.remove(selectedRoute)) {
                drawnRoutes.add(selectedRoute);
                mapRoutes(drawnRoutes, selectedRoute);
            }
            mRouteNameTextView.setVisibility(View.VISIBLE);
            mRouteNameTextView.setText(mSelectedRoute.getName() != null ? mSelectedRoute.getName() : "");

            enableButton(mStartButton, true);
        }
    }

    private static Route findNearestRoute(Iterable<Route> routes, Coordinate point) {
        Double nearestDistance = null;
        Route nearestRoute = null;

        ShapeSegmenter shapeSegmenter = new ShapeSegmenter.Builder().build();
        for (Route route : routes) {
            ShapeCalculator.LineStringPoint result = null;
            for (RouteLeg routeLeg: route.getLegs()) {
                ShapeCalculator.LineStringPoint pointForLeg = ((ShapeCalculator) shapeSegmenter)
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

    private void initGpsButton() {
        mGpsCenterOnUserLocationButton.setVisibility(View.VISIBLE);
        mGpsCenterOnUserLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasLocationPermissions()) {
                    // re-acquire current location and zoom/center map to that location
                    acquireCurrentLocationAndZoom(mMapController);
                } else {
                    requestLocationPermissions(new LocationPermissionsResultListener() {
                        @Override
                        public void onRequestLocationPermissionsResult(boolean locationPermissionsWereGranted) {
                            if (locationPermissionsWereGranted) {
                                acquireCurrentLocationAndZoom(mMapController);
                            } else {
                                // location-permissions were *denied* by the user...
                                // so nothing to do (until the user taps the "GPS center" button again)
                            }
                        }
                    });
                }
            }
        });
    }

    private class FollowUserLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(com.mapquest.navigation.model.location.Location location) {
            mLastLocation = location;
            markOrigin(location);
        }
    }
}
