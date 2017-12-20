package com.mapquest.navigation.sampleapp;

import com.mapquest.navigation.internal.unit.Duration;

public class Constants {
    public static final float VALID_STANDARD_LOCATION_ACCURACY_THRESHOLD_METERS = 150f;
    public static final long VALID_STANDARD_LOCATION_TIME_THRESHOLD = Duration.minutes(5).toMilliseconds();
    public static final long LOCATION_ACQUISITION_TIMEOUT = Duration.seconds(30).toMilliseconds();
}
