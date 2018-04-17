package com.mapquest.navigation.sampleapp.util;

import com.mapquest.navigation.model.location.Coordinate;

/**
 * Created by akumm16 on 4/17/18.
 */

public class CoordinateComparisonUtil {
    public static boolean areEqual(Coordinate coordinate1, Coordinate coordinate2) {
        return coordinate1.getLatitude() == coordinate2.getLatitude()
                && coordinate1.getLongitude() == coordinate2.getLongitude();
    }
}
