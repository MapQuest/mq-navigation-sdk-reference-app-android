package com.mapquest.navigation.sampleapp.location;

import android.support.annotation.Nullable;

import com.mapquest.android.commoncore.model.LatLng;

public interface CurrentLocationProvider {
    @Nullable
    LatLng getCurrentLocation();
}
