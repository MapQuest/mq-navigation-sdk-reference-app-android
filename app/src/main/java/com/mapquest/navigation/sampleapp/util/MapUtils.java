package com.mapquest.navigation.sampleapp.util;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class MapUtils {

    public static com.mapquest.android.commoncore.model.LatLng toMapQuestLatLng(LatLng latLng) {
        return com.mapquest.android.commoncore.model.LatLng.toValidClamp((float) latLng.getLatitude(), (float) latLng.getLongitude());
    }

    public static LatLng toMapBoxLatLng(com.mapquest.android.commoncore.model.LatLng latLng) {
        return new LatLng(latLng.getLatitude(), latLng.getLongitude());
    }

}
