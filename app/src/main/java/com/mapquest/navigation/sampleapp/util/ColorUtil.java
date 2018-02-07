package com.mapquest.navigation.sampleapp.util;

import android.graphics.Color;
import android.support.annotation.FloatRange;

import com.mapquest.navigation.model.CongestionSpan;
import com.mapquest.navigation.model.CongestionSpan.Severity;

import static com.mapquest.navigation.internal.collection.CollectionsUtil.asArray;

public final class ColorUtil {
    private static final int UNKNOWN_CONGESTION_COLOR = darkenColor(Color.BLUE, 0.15);
    private static final int FREE_FLOW_CONGESTION_COLOR = darkenColor(Color.GREEN, 0.15);
    private static final int SLOW_CONGESTION_COLOR = darkenColor(Color.YELLOW, 0.15);
    private static final int STOP_AND_GO_CONGESTION_COLOR = darkenColor(Color.RED, 0.15);
    private static final int CLOSED_CONGESTION_COLOR = darkenColor(Color.WHITE, 0.85); // Gray

    private ColorUtil() { }

    public static int hsvToColor(float hue, int saturation, int value) {
        return hsvToColor(hue, saturation/100f, value/100f);
    }

    public static int hsvToColor(float hue, float saturation, float value) {
        return Color.HSVToColor(asArray(hue, saturation, value));
    }

    public static int darkenColor(int color, double valueChange) {
        double valueMultiplier = 1.0 - valueChange; // "Value" in the HSV sense

        return Color.argb(
                Color.alpha(color),
                (int) (Color.red(color) * valueMultiplier),
                (int) (Color.green(color) * valueMultiplier),
                (int) (Color.blue(color) * valueMultiplier));
    }

    public static int setOpacity(int color, @FloatRange(from = 0, to = 1) double opacity) {
        return Color.argb(
                (int) (opacity * 255),
                Color.red(color),
                Color.green(color),
                Color.blue(color));
    }

    public static int getCongestionColor(CongestionSpan span) {
        if(span == null) {
            return UNKNOWN_CONGESTION_COLOR;
        } else if(Severity.FREE_FLOW.equals(span.getSeverity())) {
            return FREE_FLOW_CONGESTION_COLOR;
        } else if(Severity.SLOW.equals(span.getSeverity())) {
            return SLOW_CONGESTION_COLOR;
        } else if(Severity.STOP_AND_GO.equals(span.getSeverity())) {
            return STOP_AND_GO_CONGESTION_COLOR;
        } else if(Severity.CLOSED.equals(span.getSeverity())) {
            return CLOSED_CONGESTION_COLOR;
        }

        return UNKNOWN_CONGESTION_COLOR;
    }
}
