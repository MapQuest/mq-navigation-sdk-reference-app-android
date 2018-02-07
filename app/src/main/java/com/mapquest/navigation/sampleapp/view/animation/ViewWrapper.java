package com.mapquest.navigation.sampleapp.view.animation;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

/**
 * Wraps a View in order to expose its layout parameters in a way that they can be used directly
 * with a ObjectAnimator.
 */
public class ViewWrapper {
    private ViewGroup.LayoutParams mLayoutParams;
    private ViewParent mViewParent;

    public ViewWrapper(View view) {
        mLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        mViewParent = view.getParent();
    }

    public int getHeight() {
        return mLayoutParams.height;
    }

    public void setHeight(int height) {
        mLayoutParams.height = height;

        mViewParent.requestLayout();
    }

    public float getWeight() {
        return getLinearLayoutParams().weight;
    }

    @SuppressWarnings("unused")
    public void setWeight(float weight) {
        getLinearLayoutParams().weight = weight;

        mViewParent.requestLayout();
    }

    protected LinearLayout.LayoutParams getLinearLayoutParams() {
        return (LinearLayout.LayoutParams) mLayoutParams;
    }
}
