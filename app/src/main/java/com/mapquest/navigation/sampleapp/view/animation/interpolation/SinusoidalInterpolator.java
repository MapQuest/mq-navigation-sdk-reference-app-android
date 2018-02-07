package com.mapquest.navigation.sampleapp.view.animation.interpolation;

import android.animation.TimeInterpolator;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class SinusoidalInterpolator implements TimeInterpolator {
    public static final SinusoidalInterpolator INSTANCE = new SinusoidalInterpolator();

    @Override
    public float getInterpolation(float input) {
        return (float) sin(input * PI * 0.5);
    }
}
