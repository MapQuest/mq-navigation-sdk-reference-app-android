package com.mapquest.navigation.sampleapp.util;

import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.model.Maneuver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DrawableMappingUtil {
    public static Map<Maneuver.Type,Integer> buildManeuverDrawableIdMapping() {
        Map<Maneuver.Type,Integer> drawableIdsByManeuverType = new HashMap<>();

        drawableIdsByManeuverType.put(Maneuver.Type.LEFT_UTURN, R.drawable.navatar_uturn_left);
        drawableIdsByManeuverType.put(Maneuver.Type.SHARP_LEFT, R.drawable.navatar_sharp_left);
        drawableIdsByManeuverType.put(Maneuver.Type.LEFT, R.drawable.navatar_left);
        drawableIdsByManeuverType.put(Maneuver.Type.SLIGHT_LEFT, R.drawable.navatar_slight_left);
        drawableIdsByManeuverType.put(Maneuver.Type.STRAIGHT, R.drawable.navatar_straight);
        drawableIdsByManeuverType.put(Maneuver.Type.SLIGHT_RIGHT, R.drawable.navatar_slight_right);
        drawableIdsByManeuverType.put(Maneuver.Type.RIGHT, R.drawable.navatar_right);
        drawableIdsByManeuverType.put(Maneuver.Type.SHARP_RIGHT, R.drawable.navatar_sharp_right);
        drawableIdsByManeuverType.put(Maneuver.Type.RIGHT_UTURN, R.drawable.navatar_uturn_right);

        drawableIdsByManeuverType.put(Maneuver.Type.MERGE, R.drawable.navatar_merge);
        drawableIdsByManeuverType.put(Maneuver.Type.LEFT_MERGE, R.drawable.navatar_merge_left);
        drawableIdsByManeuverType.put(Maneuver.Type.RIGHT_MERGE, R.drawable.navatar_merge_right);

        drawableIdsByManeuverType.put(Maneuver.Type.LEFT_OFF_RAMP, R.drawable.navatar_exit_left);
        drawableIdsByManeuverType.put(Maneuver.Type.RIGHT_OFF_RAMP, R.drawable.navatar_exit_right);
        drawableIdsByManeuverType.put(Maneuver.Type.LEFT_FORK, R.drawable.navatar_fork_left);
        drawableIdsByManeuverType.put(Maneuver.Type.RIGHT_FORK, R.drawable.navatar_fork_right);

        return Collections.unmodifiableMap(drawableIdsByManeuverType);
    }
}
