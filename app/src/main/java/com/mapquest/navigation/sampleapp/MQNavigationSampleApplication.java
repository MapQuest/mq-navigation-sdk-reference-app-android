package com.mapquest.navigation.sampleapp;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapquest.mapping.MapQuest;
import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.sampleapp.location.MapzenLocationProviderAdapter;

public class MQNavigationSampleApplication extends Application {
    @NonNull
    private LocationProviderAdapter mLocationProviderAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        MapQuest.start(getApplicationContext());

        mLocationProviderAdapter = new MapzenLocationProviderAdapter(this);
    }

    @NonNull
    public LocationProviderAdapter getLocationProviderAdapter() {
        return mLocationProviderAdapter;
    }

    public void initializeLocationProviderAdapter() {
        mLocationProviderAdapter.initialize();
    }
}
