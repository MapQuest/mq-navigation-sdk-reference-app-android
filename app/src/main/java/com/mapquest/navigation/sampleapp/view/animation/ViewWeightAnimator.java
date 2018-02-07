package com.mapquest.navigation.sampleapp.view.animation;

import android.animation.ObjectAnimator;
import android.view.View;

import com.mapquest.navigation.sampleapp.view.animation.interpolation.SinusoidalInterpolator;

public class ViewWeightAnimator {
    public static void animate(View view, float weight, long duration) {
        ViewWrapper wrappedView = new ViewWrapper(view);
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                wrappedView, "weight",
                wrappedView.getWeight(), weight);
        animator.setDuration(duration);
        animator.setInterpolator(SinusoidalInterpolator.INSTANCE);
        animator.start();
    }
}
