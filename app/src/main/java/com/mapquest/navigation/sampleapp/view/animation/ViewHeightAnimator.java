package com.mapquest.navigation.sampleapp.view.animation;

import android.animation.ObjectAnimator;
import android.view.View;

import com.mapquest.navigation.sampleapp.view.animation.interpolation.SinusoidalInterpolator;

public class ViewHeightAnimator {
    public static void animate(View view, int height, long duration) {
        ViewWrapper wrappedView = new ViewWrapper(view);
        ObjectAnimator animator = ObjectAnimator.ofInt(
                wrappedView, "height",
                wrappedView.getHeight(), height);
        animator.setDuration(duration);
        animator.setInterpolator(SinusoidalInterpolator.INSTANCE);
        animator.start();
    }
}
