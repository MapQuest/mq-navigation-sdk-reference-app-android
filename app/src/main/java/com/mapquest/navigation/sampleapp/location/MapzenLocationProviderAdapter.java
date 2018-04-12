package com.mapquest.navigation.sampleapp.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mapquest.navigation.internal.util.ArgumentValidator;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.model.location.Location;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.LostApiClient.ConnectionCallbacks;
import com.mapzen.android.lost.api.PendingResult;
import com.mapzen.android.lost.api.ResultCallback;
import com.mapzen.android.lost.api.Status;

import java.util.concurrent.TimeUnit;

public class MapzenLocationProviderAdapter extends LocationProviderAdapter implements ConnectionCallbacks {

    private static final long LOCATION_UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    private static final float LOCATION_UPDATE_MINIMUM_DISTANCE = 0;

    private static final String TAG = LogUtil.generateLoggingTag(MapzenLocationProviderAdapter.class);

    @NonNull
    private final LostApiClient mLostApiClient;
    @NonNull
    private final LocationListener mLocationListener;

    public MapzenLocationProviderAdapter(@NonNull Context context) {
        Log.d(TAG, "MapzenLocationProviderAdapter() : constructor");

        ArgumentValidator.assertNotNull("Context may not be null.", context);
        mLostApiClient = new LostApiClient.Builder(context).addConnectionCallbacks(this).build();
        mLocationListener = new MapzenLocationListener();
    }

    @Override
    public void requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates() isConnected: " + mLostApiClient.isConnected());

        if(mLostApiClient.isConnected()) {
            final LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(LOCATION_UPDATE_INTERVAL)
                    .setSmallestDisplacement(LOCATION_UPDATE_MINIMUM_DISTANCE)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            try {
                PendingResult<Status> result = LocationServices.FusedLocationApi.requestLocationUpdates(mLostApiClient, locationRequest, mLocationListener);
                result.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status result) {
                        Log.d(TAG, "requestLocationUpdates() status: " + result.getStatusMessage());
                        mAreLocationUpdatesRunning = true;
                    }
                });
            } catch (SecurityException e) {
                Log.e(TAG, "requestLocationUpdates: permissions exception: " + e.getMessage());
            } catch(Exception e) {
                // FIXME: hrmm catch this since sometimes (!!) for some reason we get a java.util.ConcurrentModificationException
                // FIXME: in com.mapzen.android.lost.internal.LostClientManager.reportLocationChanged ??
                //
                Log.e(TAG, "requestLocationUpdates: exception: " + e.getMessage());
            }
        } else {
            // if not yet connected... do (asynch) connect; see onConnected callback below
            Log.d(TAG, "requestLocationUpdates() : was not connected, so now connecting...");
            mLostApiClient.connect();
        }
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected() isConnected: " + mLostApiClient.isConnected() + "; mAreLocationUpdatesRunning: " + mAreLocationUpdatesRunning);

        if(!mAreLocationUpdatesRunning) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended() {
        Log.d(TAG, "onConnectionSuspended() isConnected: " + mLostApiClient.isConnected());
    }

    @Override
    protected void cancelLocationUpdates() {
        if(mLostApiClient.isConnected()) {
            PendingResult<Status> result = LocationServices.FusedLocationApi.removeLocationUpdates(mLostApiClient, mLocationListener);
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status result) {
                    Log.d(TAG, "cancelLocationUpdates() status: " + result.getStatusMessage());
                    mAreLocationUpdatesRunning = false;
                }
            });
        }
    }

    @Nullable
    @Override
    public Location getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation() isConnected: " + mLostApiClient.isConnected() + "; mAreLocationUpdatesRunning: " + mAreLocationUpdatesRunning);

        if(mLostApiClient.isConnected()) {
            try {
                android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mLostApiClient);
                if (location != null) {
                    Log.d(TAG, "getLastKnownLocation() location: " + location.getLatitude() + ", " + location.getLongitude());
                    return buildLocation(location);
                } else {
                    Log.d(TAG, "getLastKnownLocation() location: null");
                    return null;
                }
            } catch (SecurityException e) {
                Log.e(TAG, "getLastKnownLocation: permissions exception: " + e.getMessage());
                return null;
            } catch (Exception e) {
                Log.e(TAG, "getLastKnownLocation: exception: " + e.getMessage());
                return null;
            }

        } else {
            // if no connection, there's really no choice but to return null here;
            // since even if a location would be available once connected, connecting is done asynchronously
            // (e.g. see requestLocationUpdates, above)
            return null;
        }
    }

    @Override
    public void initialize() {
        Log.d(TAG, "initialize()");
        super.initialize();

        mLostApiClient.connect();
    }

    @Override
    public void deinitialize() {
        Log.d(TAG, "deinitialize()");
        super.deinitialize();

        mLostApiClient.disconnect();
        mAreLocationUpdatesRunning = false;
    }

    @Override
    public String getLocationProviderId() {
        return "mapzen-location-provider";
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

    private class MapzenLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(android.location.Location location) {
            Log.d(TAG, "onLocationChanged() location: " + location.getLatitude() + ", " + location.getLongitude());

            notifyListenersLocationChanged(buildLocation(location));
        }
    }
}
