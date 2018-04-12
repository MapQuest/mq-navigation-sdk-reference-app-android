package com.mapquest.navigation.sampleapp.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapquest.mapping.maps.MapView;
import com.mapquest.mapping.maps.RoutePolylinePresenter;
import com.mapquest.navigation.NavigationManager;
import com.mapquest.navigation.dataclient.listener.RouteResponseListener;
import com.mapquest.navigation.dataclient.listener.TrafficResponseListener;
import com.mapquest.navigation.internal.collection.CollectionsUtil;
import com.mapquest.navigation.internal.logging.AccumulatingLogger;
import com.mapquest.navigation.internal.unit.Duration;
import com.mapquest.navigation.internal.unit.Speed;
import com.mapquest.navigation.internal.util.ArgumentValidator;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.listener.DefaultNavigationProgressListener;
import com.mapquest.navigation.listener.EtaResponseListener;
import com.mapquest.navigation.listener.NavigationProgressListener;
import com.mapquest.navigation.listener.NavigationStateListener;
import com.mapquest.navigation.listener.SpeedLimitSpanListener;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.CongestionSpan;
import com.mapquest.navigation.model.EstimatedTimeOfArrival;
import com.mapquest.navigation.model.Instruction;
import com.mapquest.navigation.model.Maneuver;
import com.mapquest.navigation.model.Route;
import com.mapquest.navigation.model.RouteLeg;
import com.mapquest.navigation.model.RouteStoppedReason;
import com.mapquest.navigation.model.SpeedLimit;
import com.mapquest.navigation.model.SpeedLimitSpan;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.model.Traffic;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.model.location.Destination;
import com.mapquest.navigation.model.location.Location;
import com.mapquest.navigation.model.location.LocationObservation;
import com.mapquest.navigation.sampleapp.MQNavigationSampleApplication;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.service.NavigationNotificationService;
import com.mapquest.navigation.sampleapp.util.DrawableMappingUtil;
import com.mapquest.navigation.sampleapp.util.LocationUtil;
import com.mapquest.navigation.sampleapp.view.DirectionsListAdapter;
import com.mapquest.navigation.sampleapp.view.LaneGuidanceBar;
import com.mapquest.navigation.sampleapp.view.ManeuverApproachBar;
import com.mapquest.navigation.sampleapp.view.NarrativeAdapter;
import com.mapquest.navigation.util.ShapeSegmenter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mapquest.navigation.internal.collection.CollectionsUtil.lastValue;
import static com.mapquest.navigation.internal.logging.AccumulatingLogger.buildLegDescriptions;
import static com.mapquest.navigation.sampleapp.util.ColorUtil.getCongestionColor;
import static com.mapquest.navigation.sampleapp.util.UiUtil.buildCircleMarkerOptions;
import static com.mapquest.navigation.sampleapp.util.UiUtil.buildDownArrowMarkerOptions;
import static com.mapquest.navigation.sampleapp.util.UiUtil.toast;

public class NavigationActivity extends AppCompatActivity implements LifecycleRegistryOwner {

    private static final String ROUTE_KEY = "route";
    private static final String USER_TRACKING_CONSENT_KEY = "user_tracking_consent";

    private static final String TAG = LogUtil.generateLoggingTag(NavigationActivity.class);
    private static final int PATH_WIDTH = 5;
    private static final String DISPLAY_DEBUG_INFO_KEY = "display_debug_info";
    private static final String FOLLOWING_KEY = "following";

    private static final Map<Maneuver.Type, Integer> MANEUVER_DRAWABLE_IDS_BY_TYPE = DrawableMappingUtil.buildManeuverDrawableIdMapping();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.ROOT);
    private static final double CENTER_ON_USER_ZOOM_LEVEL = 16;
    private static final double FOLLOW_MODE_TILT_VALUE_DEGREES = 60;
    private static final Double METERS_PER_MILE = 1609.34;

    private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    private ServiceConnection mServiceConnection;
    private NavigationNotificationService mNotificationService;
    private static NavigationManager mNavigationManager;
    private LocationProviderAdapter mLocationProviderAdapter;

    private List<PolylineOptions> mRoutePolylineOptionsList = new ArrayList<>();
    private Marker mRouteStartMarker;
    private Marker mRouteEndMarker;
    private Marker mClosestRoutePointMarker;
    private Marker mUserLocationMarker;
    private List<Marker> mGuidancePromptMarkers = new ArrayList<>();
    @Nullable
    private Route mInitialRoute;

    private NavigationProgressListener mMapCenteringNavigationProgressListener = new MapCenteringNavigationProgressListener();
    private NavigationStateListener mNavigationStateListener = new UiUpdatingNavigationStateListener();
    private RouteResponseListener mRouteResponseListener = new UiUpdatingRouteResponseListener();
    private NavigationProgressListener mNavigationProgressListener = new UiUpdatingNavigationProgressListener();
    private TrafficResponseListener mTrafficResponseListener = new UiUpdatingTrafficResponseListener();
    private SpeedLimitSpanListener mSpeedLimitSpanListener = new UiUpdatingSpeedLimitSpanListener();
    private EtaResponseListener mEtaResponseListener = new UiEtaResponseListener();

    @BindView(R.id.map)
    protected MapView mMap;
    private MapboxMap mMapController;
    private RoutePolylinePresenter mRoutePolylinePresenter;

    private NarrativeAdapter mDirectionsListAdapter;

    @BindView(R.id.status)
    protected TextView mStatusLabel;
    @BindView(R.id.gps_accuracy)
    protected TextView mGpsAccuracy;
    @BindView(R.id.next_maneuver)
    protected TextView mNextManeuverLabel;
    @BindView(R.id.next_maneuver_icon)
    protected ImageView mNextManeuverIcon;
    @BindView(R.id.pause_resume_icon)
    protected ImageView mPauseResumeIcon;
    @BindView(R.id.next_maneuver_distance)
    protected TextView mNextManeuverDistanceLabel;
    @BindView(R.id.eta_next_destination)
    protected TextView mEtaNextDestinationLabel;
    @BindView(R.id.eta_final_destination)
    protected TextView mEtaFinalDestinationLabel;
    @BindView(R.id.remaining_distance)
    protected TextView mRemainingDistanceLabel;

    @BindView(R.id.main_nav_map_content)
    protected RelativeLayout mMainNavMapLayout;
    @BindView(R.id.directions_list_layout)
    protected RelativeLayout mDirectionsListLayout;

    @BindView(R.id.directions_list_toggle_button_text_view)
    protected TextView mDirectionsListToggleButton;
    @BindView(R.id.directions_list_view)
    protected ListView mDirectionsListView;

    @BindView(R.id.left_maneuver_approach_bar)
    protected ManeuverApproachBar mLeftManeuverApproachBar;
    @BindView(R.id.right_maneuver_approach_bar)
    protected ManeuverApproachBar mRightManeuverApproachBar;

    @BindView(R.id.max_speed_limit_label)
    protected TextView mMaxSpeedLimitLabel;
    @BindView(R.id.advisory_max_speed_limit_label)
    protected TextView mAdvisoryMaxSpeedLimitLabel;
    @BindView(R.id.school_zone_max_speed_limit_label)
    protected TextView mSchoolZoneMaxSpeedLimitLabel;
    @BindView(R.id.maneuver_lanes)
    LaneGuidanceBar mLaneGuidanceBar;
    @BindView(R.id.skipLegIcon)
    protected ImageView mSkipLegImageView;

    @OnClick(R.id.directions_list_toggle_button_text_view)
    protected void toggleDirectionsList() {
        if (mMainNavMapLayout.getVisibility() == View.VISIBLE) {
            mMainNavMapLayout.setVisibility(View.GONE);
            mDirectionsListLayout.setVisibility(View.VISIBLE);
            mDirectionsListToggleButton.setText(R.string.map_view_title);
        } else {
            mMainNavMapLayout.setVisibility(View.VISIBLE);
            mDirectionsListLayout.setVisibility(View.GONE);
            mDirectionsListToggleButton.setText(R.string.list_view_title);
        }
    }

    @OnClick(R.id.gps_center_on_user_location_button_navigation)
    protected void centerOnUserLocation() {
        Log.d(TAG, "centerOnUserLocation() mLastLocationObservation: " + mLastLocationObservation);

        if (mLastLocationObservation != null) {
            mMapController.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocationObservation.getSnappedLocation().getLatitude(),
                            mLastLocationObservation.getSnappedLocation().getLongitude()),
                    CENTER_ON_USER_ZOOM_LEVEL));
            enterFollowMode();

        } else {
            // no location... so attempt to (re-)acquire current location, then zoom/center on that
            LocationUtil.acquireLocation(this, mLocationProviderAdapter, new LocationProviderAdapter.LocationAcquisitionListener() {
                @Override
                public void onLocationAcquired(Location acquiredLocation) {
                    mMapController.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(acquiredLocation.getLatitude(), acquiredLocation.getLongitude()),
                            CENTER_ON_USER_ZOOM_LEVEL));
                    enterFollowMode();
                }
            });
        }
    }

    @OnClick(R.id.skipLegIcon)
    void handleSkipToNextLegClick() {
        mNavigationManager.advanceRouteToNextLeg();
        showSkipLegButton(!isNavigatingFinalRouteLeg());
    }


    @BindView(R.id.follow)
    protected TextView mFollowButton;
    private boolean mFollowing;

    private ProgressDialog mProgressDialog;

    private Route mRoute;

    private boolean mShouldRestoreFollowMode;
    private LocationObservation mLastLocationObservation;
    private boolean mUserTrackingConsentGranted;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            mRoute = getIntent().getExtras().getParcelable(ROUTE_KEY);
            mUserTrackingConsentGranted = getIntent().getExtras().getBoolean(USER_TRACKING_CONSENT_KEY);
        }
        mShouldRestoreFollowMode = (savedInstanceState == null) || savedInstanceState.getBoolean(FOLLOWING_KEY);

        mLocationProviderAdapter = ((MQNavigationSampleApplication) getApplication()).getLocationProviderAdapter();

        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapController) {
                // once MapView is ready...
                mMapController = mapController;
                mRoutePolylinePresenter = new RoutePolylinePresenter(mMap, mMapController);
                mMap.setOnTouchListener(new FollowModeExitingMapTouchListener());

                setZoomLevel(16);
                if (mInitialRoute != null) {
                    mapRoute(mInitialRoute);
                }
                mInitialRoute = null;

                // bind to our Navigation Service (which provides and manages a NavigationManager instance)
                mServiceConnection = initializeNavigationService(mRoute);
            }
        });

        mPauseResumeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNavigationManager != null) {
                    if (mNavigationManager.getNavigationState() != NavigationManager.NavigationState.PAUSED) {
                        mNavigationManager.pauseNavigation();
                        mPauseResumeIcon.setImageResource(R.drawable.ic_play);
                        final AlertDialog alertDialog = new AlertDialog.Builder(NavigationActivity.this)
                                .setTitle("Navigation Paused")
                                .setMessage("Navigation has been PAUSED. Click OK to resume navigation...")
                                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mNavigationManager.resumeNavigation();
                                        mPauseResumeIcon.setImageResource(R.drawable.ic_pause);
                                    }
                                })
                                .create();
                        alertDialog.show();
                    } else {
                        mNavigationManager.resumeNavigation();
                        mPauseResumeIcon.setImageResource(R.drawable.ic_pause);
                        Toast.makeText(getApplicationContext(), "Navigation resumed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void setDirectionsList(List<Instruction> instructions) {
        mDirectionsListToggleButton.setVisibility(View.VISIBLE);
        mDirectionsListAdapter = new DirectionsListAdapter(this);
        mDirectionsListAdapter.setData(instructions);
        mDirectionsListView.setAdapter(mDirectionsListAdapter);
    }

    public void updateDirectionsList(List<Instruction> instructions) {
        if (mDirectionsListAdapter != null) {
            mDirectionsListAdapter.setData(instructions);
            mDirectionsListAdapter.notifyDataSetChanged();
        }
    }

    private void setZoomLevel(int level) {
        mMapController.moveCamera(CameraUpdateFactory.newCameraPosition(
                createUpdatedCameraPositionFromCurrent(level)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        mMap.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private ServiceConnection initializeNavigationService(@NonNull Route route) {
        ArgumentValidator.assertNotNull(route);

        Log.d(TAG, "initializeNavigationService()");
        displayProgressDialog("Starting Navigation", "Starting navigation. One moment...");

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {

                Log.d(TAG, "onServiceConnected()");
                dismissProgressDialog();

                mNotificationService = NavigationNotificationService.fromBinder(binder);

                mNavigationManager = mNotificationService.getNavigationManager();

                addNavigationListeners(mNavigationManager);

                mNavigationManager.startNavigation(mRoute);
                if(mShouldRestoreFollowMode) {
                    enterFollowMode();
                }
                updateDirectionsList();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected()");

                mNavigationManager.deinitialize();
                mNavigationManager = null;
                mNotificationService = null;
            }
        };

        Intent navigationActivityIntent = buildNavigationActivityIntent(getApplicationContext(), mRoute,
                mUserTrackingConsentGranted);

        PendingIntent notificationContentIntent = PendingIntent.getActivity(getApplicationContext(),
                0, navigationActivityIntent, 0);

        Intent serviceIntent = NavigationNotificationService.buildNavigationNotificationServiceIntent(
                getApplicationContext(), route.getRouteOptions().getLanguage(), mUserTrackingConsentGranted,
                notificationContentIntent);

        startService(serviceIntent); // note: we both start *and* bind to svc to keep it running even if activity is killed
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        return serviceConnection;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        mMap.onResume();
        if(mNavigationManager != null) {
            updateDirectionsList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mMap.onSaveInstanceState(outState);
        outState.putBoolean(FOLLOWING_KEY, mFollowing);

        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("MissingPermission") // If we're already navigating, we must have the permission
    @Override
    protected void onPause() {
        super.onPause();

        mMap.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");

        mMap.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        // unregister listeners that hold view references
        exitFollowMode();
        removeNavigationListeners();

        mMap.onDestroy();

        // finally, gracefully dispose of Service (and its NavigationManager)
        unbindService(mServiceConnection);

        super.onDestroy();
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    private void updateDirectionsList() {
        RouteLeg currentRouteLeg = mNavigationManager.getCurrentRouteLeg();
        if (currentRouteLeg != null) {
            List<Instruction> instructionList = currentRouteLeg.getInstructions();
            if (instructionList != null) {
                setDirectionsList(instructionList);
            }
        } else {
            Log.e(TAG, "Navigation Manager does not currently contain a route leg.");
        }
    }

    @OnClick(R.id.stop)
    void handleStopClick() {
        NavigationManager navigationManager = mNavigationManager;
        if(navigationManager.getNavigationState() != NavigationManager.NavigationState.STOPPED) {
            navigationManager.cancelNavigation();
        }

        finish();
    }

    private void addNavigationListeners(final NavigationManager manager) {
        manager.addNavigationStateListener(mNavigationStateListener);
        manager.addAndNotifyProgressListener(mNavigationProgressListener);
        manager.addTrafficResponseListener(mTrafficResponseListener);
        manager.addAndNotifySpeedLimitSpanListener(mSpeedLimitSpanListener);
        manager.addEtaResponseListener(mEtaResponseListener);
    }

    private void removeNavigationListeners() {
        mNavigationManager.removeNavigationStateListener(mNavigationStateListener);
        mNavigationManager.removeProgressListener(mNavigationProgressListener);
        mNavigationManager.removeTrafficResponseListener(mTrafficResponseListener);
        mNavigationManager.removeSpeedLimitSpanListener(mSpeedLimitSpanListener);
        mNavigationManager.removeEtaResponseListener(mEtaResponseListener);
    }
    
    private String formatTimestampForLocalTimezone(long timestamp) {
        return TIME_FORMAT.format(new Date(timestamp));
    }

    private void resetUi() {
        mLaneGuidanceBar.setLanes(null);
        // TODO Hide turn indicators?
    }

    private String buildMessageForMissingTrafficUpdate(String legId, Set<String> updateLegIds) {
        StringBuilder messageStringBuilder = new StringBuilder("No traffic update for leg with ID ")
                .append(legId)
                .append(". Legs present in response were: ");

        boolean firstPass = true;
        for (String updateLegId : updateLegIds) {
            messageStringBuilder.append(firstPass ? "" : ", ")
                    .append(updateLegId);

            firstPass = false;
        }
        return messageStringBuilder.toString();
    }

    private String formatSpeed(float speed, SystemOfMeasurement localSystemOfMeasurement) {
        return SystemOfMeasurement.METRIC.equals(localSystemOfMeasurement) ?
                Math.round(Speed.metersPerSecond(speed).toKilometersPerHour()) + "\nKPH" :
                Math.round(Speed.metersPerSecond(speed).toMilesPerHour()) + "\nMPH";
    }

    private void mapPath(RouteLeg routeLeg, List<CongestionSpan> trafficConditions) {
        if(mMapController == null) {
            return;
        }
        List<ShapeSegmenter.SpanPathPair<CongestionSpan>> segments = new ShapeSegmenter.Builder().build().segmentPath(routeLeg.getShape(), trafficConditions);
        for(ShapeSegmenter.SpanPathPair<CongestionSpan> segment : segments) {
            int color = getCongestionColor(segment.getSpan());

            PolylineOptions polylineOptions = mapPathSegment(segment.getShapeCoordinates(), color, PATH_WIDTH);
            mRoutePolylineOptionsList.add(polylineOptions);
        }
    }

    private void updateUserLocationMarker(Location location) {
        clearUserLocationMarker();

        if(mMapController != null) {
            mUserLocationMarker = mMapController.addMarker(buildCircleMarkerOptions(this, android.R.color.white)
                    .position(toLatLng(location)));
        }
    }

    private void clearUserLocationMarker() {
        if((mUserLocationMarker != null) && (mMapController != null)) {
            mMapController.removeMarker(mUserLocationMarker);
            mUserLocationMarker = null;
        }
    }

    private void updateClosestRoutePoint(Coordinate closestRoutePoint) {
        if(mClosestRoutePointMarker != null) {
            clearClosestRoutePointMarker();
        }
        if(mMapController != null) {
            mClosestRoutePointMarker = mMapController.addMarker(buildCircleMarkerOptions(this, R.color.marker_yellow)
                    .position(toLatLng(closestRoutePoint)));
        }
    }

    @OnClick(R.id.follow)
    protected synchronized void enterFollowMode() {
        Log.d(TAG, "enterFollowMode() mFollowing: " + mFollowing);

        if(mNavigationManager == null) {
            return;
        }
        if(!mFollowing) {
            mNavigationManager.addProgressListener(mMapCenteringNavigationProgressListener);

            mFollowButton.setVisibility(View.GONE);
            mFollowing = true;

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "enterFollowMode() delayed runnable; mMapController: " + mMapController + " mLastLocationObservation: " + mLastLocationObservation);
                    if((mMapController != null) && (mLastLocationObservation != null)) {
                        moveZoomAndTiltMap(
                                mLastLocationObservation.getSnappedLocation().getLatitude(),
                                mLastLocationObservation.getSnappedLocation().getLongitude(),
                                CENTER_ON_USER_ZOOM_LEVEL, FOLLOW_MODE_TILT_VALUE_DEGREES
                        );
                    }
                }
            }, 600); // do this 600ms later so that mapController and last-location will be non-null
        }
    }

    private synchronized void exitFollowMode() {
        Log.d(TAG, "exitFollowMode() mFollowing: " + mFollowing);

        if(mNavigationManager == null) {
            return;
        }
        if(mFollowing) {
            mNavigationManager.removeProgressListener(mMapCenteringNavigationProgressListener);
            if (mLastLocationObservation != null) {
                moveZoomAndTiltMap(
                        mLastLocationObservation.getSnappedLocation().getLatitude(),
                        mLastLocationObservation.getSnappedLocation().getLongitude(),
                        CENTER_ON_USER_ZOOM_LEVEL, 0
                );
            }
            mFollowButton.setVisibility(View.VISIBLE);
            mFollowing = false;
        }
    }

    private void clearMarkup() {
        clearRouteEndMarkers();
        clearGuidancePromptMarkers();
        clearRoutePath();
        clearClosestRoutePointMarker();
        clearUserLocationMarker();
    }

    private void mapRoute(Route route) {
        clearMarkup();
        if(mMapController == null) {
            mInitialRoute = route;
            return;
        }

        // map all route-leg segments (with path colors based on traffic-conditions)
        clearRoutePath();
        List<RouteLeg> routeLegs = route.getLegs();
        for (RouteLeg leg: routeLegs) {
            // draw leg destination markers: intermediate destinations are blue; final is red marker
            int marker_color = (routeLegs.indexOf(leg) == routeLegs.size() - 1) ? R.color.marker_red : R.color.marker_blue;
            mRouteEndMarker = mMapController.addMarker(buildDownArrowMarkerOptions(this, marker_color)
                    .position(toLatLng(lastValue(leg.getShape().getCoordinates()))));

            // draw a route-leg path
            mapPath(leg, leg.getTraffic().getConditions());
        }
    }

    private PolylineOptions mapPathSegment(List<Coordinate> path, int color, int lineWidth) {
        PolylineOptions options = new PolylineOptions();
        options.color(color);
        options.width(lineWidth);

        for(Coordinate point : path) {
            options.add(toLatLng(point));
        }
        mRoutePolylinePresenter.addPolyline(options);
        return options;
    }

    private void clearClosestRoutePointMarker() {
        if((mClosestRoutePointMarker != null) && (mMapController != null)) {
            mMapController.removeMarker(mClosestRoutePointMarker);
            mClosestRoutePointMarker = null;
        }
    }

    private void clearGuidancePromptMarkers() {
        if(mMapController == null) {
            return;
        }
        for(Marker marker : mGuidancePromptMarkers) {
            mMapController.removeMarker(marker);
        }
        mGuidancePromptMarkers.clear();
    }

    private void clearRouteEndMarkers() {
        if((mRouteStartMarker != null) && (mMapController != null)) {
            mMapController.removeMarker(mRouteStartMarker);
            mRouteStartMarker = null;
        }

        if((mRouteEndMarker != null) && (mMapController != null)) {
            mMapController.removeMarker(mRouteEndMarker);
            mRouteEndMarker = null;
        }
    }

    private void clearRoutePath() {
        if(mMapController == null) {
            return;
        }
        for(PolylineOptions polylineOptions : new ArrayList<>(mRoutePolylineOptionsList)) {
            mRoutePolylinePresenter.removePolyline(polylineOptions);
            mRoutePolylineOptionsList.remove(polylineOptions);
        }
    }

    private void displayProgressDialog(String title, String message) {
        dismissProgressDialog();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void updateNextManeuverIcon(Maneuver maneuver) {
        if(maneuver == null || !MANEUVER_DRAWABLE_IDS_BY_TYPE.containsKey(maneuver.getType())) {
            mNextManeuverIcon.setVisibility(View.INVISIBLE);
        } else {
            mNextManeuverIcon.setImageResource(MANEUVER_DRAWABLE_IDS_BY_TYPE.get(maneuver.getType()));
            mNextManeuverIcon.setVisibility(View.VISIBLE);
        }
    }

    private void updateNextManeuverLabel(Maneuver maneuver) {
        if(maneuver == null) {
            mNextManeuverLabel.setText("");
        } else {
            String labelText = ((maneuver.getName() != null) && !maneuver.getName().trim().isEmpty()) ?
                    maneuver.getTypeText() + ", " + maneuver.getName() :
                    maneuver.getTypeText();
            mNextManeuverLabel.setText(labelText);
        }
    }

    private void updateNextManeuverDistanceLabel(Double distance, SystemOfMeasurement systemOfMeasurement, String languageCode) {
        String maneuverDistance = maneuverDistanceString(distance, systemOfMeasurement, (languageCode != null) ? languageCode : "en_US");
        mNextManeuverDistanceLabel.setText(maneuverDistance);
    }
    private String maneuverDistanceString(Double distance, SystemOfMeasurement systemOfMeasurement, String languageCode) {
        if (distance == null) {
            return "";
        }
        if (systemOfMeasurement.equals(SystemOfMeasurement.METRIC)) {
            return getResources().getString(languageCode.equals("en_US") ?
                    R.string.maneuver_distance_metric : R.string.maneuver_distance_metric_es, distance.intValue());
        } else {
            return getResources().getString(languageCode.equals("en_US") ?
                    R.string.maneuver_distance_imperial : R.string.maneuver_distance_imperial_es, distance / METERS_PER_MILE);
        }
    }

    private void updateStatusLabel(String status) {
        mStatusLabel.setText(status);
    }

    private static LatLng toLatLng(Coordinate coordinate) {
        return new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
    }

    public static Intent buildNavigationActivityIntent(Context context, Route route, boolean userTrackingConsentGranted) {
        Intent intent = new Intent(context, NavigationActivity.class);
        intent.putExtra(ROUTE_KEY, route);
        intent.putExtra(USER_TRACKING_CONSENT_KEY, userTrackingConsentGranted);
        return intent;
    }

    private void moveZoomAndTiltMap(double latitude, double longitude, double zoom, double tilt) {
        if(mMapController == null) {
            return;
        }
        CameraPosition position = new CameraPosition.Builder(mMapController.getCameraPosition())
                .target(new LatLng(latitude, longitude))
                .zoom(zoom)
                .tilt(tilt)
                .build();

        mMapController.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    private void animateCamera(double latitude, double longitude, double bearing, double zoom, double tilt) {
        if(mMapController == null) {
            return;
        }
        mMapController.animateCamera(
                CameraUpdateFactory.newCameraPosition(createUpdatedCameraPositionFromCurrent(latitude, longitude, bearing, zoom, tilt)),
                (int) Duration.seconds(1).toMilliseconds());
    }

    private CameraPosition createUpdatedCameraPositionFromCurrent(double zoomLevel) {
        return new CameraPosition.Builder(mMapController.getCameraPosition())
                .zoom(zoomLevel)
                .build();
    }

    private CameraPosition createUpdatedCameraPositionFromCurrent(double latitude, double longitude, double bearing, double zoom, double tilt) {
        return new CameraPosition.Builder(mMapController.getCameraPosition())
                .target(new LatLng(latitude, longitude))
                .bearing(bearing)
                .zoom(zoom)
                .tilt(tilt)
                .build();
    }

    private void updateRouteUi(Route route) {
        if ((route != null)) {
            mapRoute(route);
            for (RouteLeg routeLeg : route.getLegs()) {
                Log.d(TAG, "updateRouteUi() ETA for route leg " + routeLeg.getId() + " : " +
                        formatTimestampForLocalTimezone(routeLeg.getTraffic().getEstimatedTimeOfArrival().getTime()));
            }
            try {
                Traffic lastRouteLegTraffic = route.getLegs().get(CollectionsUtil.lastIndex(route.getLegs())).getTraffic();
                mEtaFinalDestinationLabel.setText(
                        formatTimestampForLocalTimezone(lastRouteLegTraffic.getEstimatedTimeOfArrival().getTime()));
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "updateRouteUi() failed to get ETA for last route leg");
            }
        }

        RouteLeg currentRouteLeg = mNavigationManager.getCurrentRouteLeg();
        if (currentRouteLeg != null) {
            Traffic currentRouteLegTraffic = currentRouteLeg.getTraffic();
            mEtaNextDestinationLabel.setText(
                    formatTimestampForLocalTimezone(currentRouteLegTraffic.getEstimatedTimeOfArrival().getTime()));
        } else {
            Log.e(TAG, "updateRouteUi() failed to get ETA for current route leg");
        }

        resetUi();
    }

    private boolean isNavigatingFinalRouteLeg() {
        Route currentRoute = mNavigationManager.getRoute();
        int currentLegIndex = currentRoute.getLegs().indexOf(mNavigationManager.getCurrentRouteLeg());
        return currentLegIndex >= (currentRoute.getLegs().size() - 1);
    }

    private void showSkipLegButton(boolean enabled) {
        mSkipLegImageView.setImageResource(enabled ? R.drawable.ic_skip_next_black_24dp : R.drawable.ic_skip_next_gray_24dp);
    }

    private class DismissDialogOnClickListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /**
     * Progress listener that re-centers the map on the user's current location as location updates
     * are received. Added when entering follow-mode, removed when exiting follow-mode.
     */
    private class MapCenteringNavigationProgressListener extends DefaultNavigationProgressListener {

        @Override
        public void onLocationObservationReceived(LocationObservation locationObservation) {
            mLastLocationObservation = locationObservation;
            animateCamera(
                    locationObservation.getRawGpsLocation().getLatitude(),
                    locationObservation.getRawGpsLocation().getLongitude(),
                    locationObservation.getRawGpsLocation().getBearing(),
                    CENTER_ON_USER_ZOOM_LEVEL,
                    FOLLOW_MODE_TILT_VALUE_DEGREES);
        }
    }

    private class UiUpdatingNavigationStateListener implements NavigationStateListener {

        @Override
        public void onNavigationStarted() {
            updateStatusLabel("Starting");
            showSkipLegButton(!isNavigatingFinalRouteLeg());

            Route route = mNavigationManager.getRoute();
            updateRouteUi(route);

            if (mNavigationManager.getRoute() != null) {
                updateDirectionsList(mNavigationManager.getRoute().getLeg(0).getInstructions());
            } else {
                updateStatusLabel("Failed to start");
            }

            AccumulatingLogger.INSTANCE.addItem("Navigation started");
            AccumulatingLogger.INSTANCE.addItem("Accepted route with legs " + buildLegDescriptions(route.getLegs()));
        }

        @Override
        public void onNavigationStopped(@NonNull RouteStoppedReason routeStoppedReason) {
            switch (routeStoppedReason) {
                case ROUTE_CANCELED:
                    updateStatusLabel("Canceled");
                    break;
                case ROUTE_COMPLETED:
                    updateStatusLabel("Complete");
                    AccumulatingLogger.INSTANCE.addItem("Navigation completed");
                    break;
                default:
                    updateStatusLabel("Stopped");
            }
        }

        @Override
        public void onNavigationPaused() {
            updateStatusLabel("Paused");
        }

        @Override
        public void onNavigationResumed() {
            updateStatusLabel("Resumed (navigating)");
        }
    }


    private class UiUpdatingRouteResponseListener implements RouteResponseListener {
        @Override
        public void onRequestMade() {
            Toast.makeText(getApplicationContext(), "Off Route. Requesting updated route...", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRouteRetrieved(Route route) {
            updateRouteUi(route);
            updateDirectionsList();
        }

        @Override
        public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {
            toast(NavigationActivity.this, "Route request failed.");
        }
    }

    private class UiUpdatingNavigationProgressListener implements NavigationProgressListener {

        @Override
        public void onLocationObservationReceived(LocationObservation locationObservation) {
            mLastLocationObservation = locationObservation;
            updateUserLocationMarker(locationObservation.getRawGpsLocation());
            updateClosestRoutePoint(locationObservation.getSnappedLocation());
            updateNextManeuverDistanceLabel(
                    locationObservation.getDistanceToUpcomingManeuver(),
                    mRoute.getRouteOptions().getSystemOfMeasurementForDisplayText(),
                    mRoute.getRouteOptions().getLanguage()
            );

            Double distanceToUpcomingManeuver = locationObservation.getDistanceToUpcomingManeuver();
            if (distanceToUpcomingManeuver != null) {
                mLeftManeuverApproachBar.update(distanceToUpcomingManeuver);
                mRightManeuverApproachBar.update(distanceToUpcomingManeuver);
            }

            mGpsAccuracy.setText((int) locationObservation.getRawGpsLocation().getAccuracy() + "m Accuracy");
            mRemainingDistanceLabel.setText(((int) locationObservation.getRemainingLegDistance()) + " meters");
        }

        @Override
        public void onUpcomingManeuverUpdated(Maneuver upcomingManeuver) {
            updateNextManeuverIcon(upcomingManeuver);
            updateNextManeuverLabel(upcomingManeuver);

            mLeftManeuverApproachBar.update(upcomingManeuver);
            mRightManeuverApproachBar.update(upcomingManeuver);
            mLaneGuidanceBar.setLanes(upcomingManeuver);
        }

        @Override
        public void onDestinationReached(@NonNull Destination destination, boolean isFinalDestination,
                                         @NonNull RouteLeg routeLegCompleted,
                                         @NonNull final DestinationAcceptanceHandler destinationAcceptanceHandler) {
            if (!isFinalDestination) {

                final AlertDialog alertDialog = new AlertDialog.Builder(NavigationActivity.this)
                        .setTitle("Arrived at Waypoint")
                        .setMessage("Intermediate destination reached. Press 'Proceed' to proceed to the next stop...")
                        .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                destinationAcceptanceHandler.confirmArrival(true);
                                updateDirectionsList();

                                // allow user to skip ahead only if not on final leg of route
                                showSkipLegButton(!isNavigatingFinalRouteLeg());
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                destinationAcceptanceHandler.confirmArrival(false);
                            }
                        })
                        .show();

            } else {
                clearMarkup();

                final AlertDialog alertDialog = new AlertDialog.Builder(NavigationActivity.this)
                        .setTitle("Done Navigating")
                        .setMessage("Final destination reached")
                        .setPositiveButton("OK", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                destinationAcceptanceHandler.confirmArrival(true);
                                finish(); // OK, done with NavigationActivity...
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                destinationAcceptanceHandler.confirmArrival(false);
                            }
                        })
                        .show();
            }
        }

        @Override
        public void onInaccurateObservationReceived(Location location) { }
    }

    private class UiUpdatingTrafficResponseListener implements TrafficResponseListener {
        @Override
        public void onRequestMade() {
            toast(NavigationActivity.this, "Traffic update request made");
            mEtaNextDestinationLabel.setAlpha(0.5f);
            mEtaFinalDestinationLabel.setAlpha(0.5f);
        }

        @Override
        public void onTrafficUpdated(Map<String, Traffic> trafficUpdatesByLegId) {
            toast(NavigationActivity.this, "Traffic updated");

            RouteLeg routeLeg = mNavigationManager.getCurrentRouteLeg();
            if (routeLeg == null) {
                Log.e(TAG, "Current route leg is null. Cannot update traffic.");
                return;
            }

            Traffic update = trafficUpdatesByLegId.get(routeLeg.getId());
            try{
                // note: null-check on update is needed since it's still possible to get a response for a routeLeg that's no longer in effect
                if(update != null) {
                    mapPath(routeLeg, update.getConditions());
                }
            } catch(IndexOutOfBoundsException exception) {
                AccumulatingLogger.INSTANCE.addItemAndLog(
                        "Invalid congestion information for routeLeg " + routeLeg.getId(),
                        exception);

                throw exception;
            }
        }

        @Override
        public void onTrafficRerouteFound(Route route) {
            displayRerouteDialog(mNavigationManager.getRoute(), route);
        }

        @Override
        public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {
            toast(NavigationActivity.this, "Traffic update failed");
        }
    }

    private void displayRerouteDialog(Route currentRoute, final Route trafficReroute) {
        long etaDifference = lastValue(currentRoute.getLegs()).getTraffic().getEstimatedTimeOfArrival().getTime() -
                lastValue(trafficReroute.getLegs()).getTraffic().getEstimatedTimeOfArrival().getTime();

        double minutes = Duration.milliseconds(etaDifference).toMinutes();

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNavigationManager.startNavigation(trafficReroute);
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Why so slow?")
                .setMessage(String.format(Locale.US, "There's a faster route that will save you %1.1f minutes. Take it?", minutes))
                .setPositiveButton("Of course!", listener)
                .setNegativeButton("Nah, I'm good.", new DismissDialogOnClickListener())
                .show();
    }

    private class UiUpdatingSpeedLimitSpanListener implements SpeedLimitSpanListener {
        @Override
        public void onSpeedLimitBoundariesCrossed(@NonNull Set<SpeedLimitSpan> exitedSpeedLimits, @NonNull Set<SpeedLimitSpan> enteredSpeedLimits) {
            for (SpeedLimitSpan speedLimitSpan : exitedSpeedLimits) {
                getView(speedLimitSpan.getSpeedLimit().getType()).setVisibility(View.GONE);
            }

            for (SpeedLimitSpan speedLimitSpan : enteredSpeedLimits) {
                TextView view = getView(speedLimitSpan.getSpeedLimit().getType());
                view.setText(formatSpeed(speedLimitSpan.getSpeedLimit().getSpeed(), speedLimitSpan.getSpeedLimit().getLocalSystemOfMeasurement()));
                view.setVisibility(View.VISIBLE);
            }
        }

        private TextView getView(SpeedLimit.Type type) {
            if(SpeedLimit.Type.MAXIMUM.equals(type)) {
                return mMaxSpeedLimitLabel;
            } else if(SpeedLimit.Type.RECOMMENDED.equals(type)) {
                return mAdvisoryMaxSpeedLimitLabel;
            } else {
                return mSchoolZoneMaxSpeedLimitLabel;
            }
        }
    }

    private class UiEtaResponseListener implements EtaResponseListener {
        @Deprecated
        @Override
        public void onEtaUpdate(@NonNull EstimatedTimeOfArrival estimatedTimeOfArrival) {}

        @Override
        public void onEtaUpdate(@NonNull Map<String, Traffic> trafficUpdatesByLegId, String currentLegId) {
            Route route = mNavigationManager.getRoute();
            if (route == null) {
                Log.e(TAG, "No route pertaining to this ETA update exists.");
                return;
            }

            String lastRouteLegKey = Integer.toString(CollectionsUtil.lastIndex(route.getLegs()));
            long etaForNextDestination = trafficUpdatesByLegId.get(currentLegId).getEstimatedTimeOfArrival().getTime();
            long etaForFinalDestination = trafficUpdatesByLegId.get(lastRouteLegKey).getEstimatedTimeOfArrival().getTime();

            mEtaNextDestinationLabel.setAlpha(1.0f);
            mEtaNextDestinationLabel.setText(formatTimestampForLocalTimezone(etaForNextDestination));

            mEtaFinalDestinationLabel.setAlpha(1.0f);
            mEtaFinalDestinationLabel.setText(formatTimestampForLocalTimezone(etaForFinalDestination));
        }

        @Override
        public void onRequestFailed(@Nullable Integer httpStatusCode, @Nullable IOException exception) {}

        @Override
        public void onRequestMade() {}
    }

    class FollowModeExitingMapTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            exitFollowMode();
            view.performClick();
            return false;
        }
    }
}
