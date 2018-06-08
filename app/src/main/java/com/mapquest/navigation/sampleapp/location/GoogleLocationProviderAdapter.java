package com.mapquest.navigation.sampleapp.location;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.location.Location;

import java.util.concurrent.TimeUnit;

/**
 * A LocationProviderAdapter that wraps Google's {@link FusedLocationProviderClient}.
 * <p>
 * Note use of this adapter requires a dependency on the Google Play Services (location) library.
 */
public class GoogleLocationProviderAdapter extends LocationProviderAdapter {

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback callback;
    private Location lastLocation;
    private boolean isInitialized;

    private static final long LOCATION_UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private static final String TAG = LogUtil.generateLoggingTag(GoogleLocationProviderAdapter.class);

    public GoogleLocationProviderAdapter(Context context) {
        this.context = context;
    }

    public void initialize() {
        Log.d(TAG, "initialize(): " + isInitialized);
        if(isInitialized) return; // don't re-initialize if already init'ed
        super.initialize();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location location = buildLocation(result.getLastLocation());
                Log.d(TAG, "onLocationResult() location: " + location.getLatitude() + ", " + location.getLongitude());
                notifyListenersLocationChanged(location);
                lastLocation = location;
            }
        };
        requestLocationUpdates();
        isInitialized = true;
    }

    public void deinitialize() {
        Log.d(TAG, "deinitialize()");
        super.deinitialize();
        isInitialized = false;
    }

    @Override
    public void requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates()");
        if(fusedLocationClient == null) return;
        try {
            fusedLocationClient.requestLocationUpdates(getLocationRequest(), callback, null);
        } catch (SecurityException e) {
            Log.e(TAG, "requestLocationUpdates: permissions exception: " + e.getMessage());
            return;
        } catch (Exception e) {
            Log.e(TAG, "requestLocationUpdates: exception: " + e.getMessage());
            return;
        }
    }

    @Override
    protected void cancelLocationUpdates() {
        Log.d(TAG, "cancelLocationUpdates()");
        if(fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(callback);
        }
    }

    @Nullable
    @Override
    public Location getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation() location: " + ((lastLocation != null) ? (lastLocation.getLatitude() + ", " + lastLocation.getLongitude()) : "null"));
        return lastLocation;
    }

    @Override
    public String getLocationProviderId() {
        return "google-location-provider";
    }


    private LocationRequest getLocationRequest() {
        LocationRequest rq = new LocationRequest();
        rq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        rq.setInterval(LOCATION_UPDATE_INTERVAL);
        rq.setFastestInterval(LOCATION_UPDATE_INTERVAL);
        return rq;
    }

    private static Location buildLocation(android.location.Location location) {
        return new Location(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getBearing(),
                location.getSpeed(),
                location.getAccuracy(),
                location.getTime());
    }
}

