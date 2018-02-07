package com.mapquest.navigation.sampleapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mapquest.navigation.sampleapp.util.UiUtil;
import com.mapquest.navigation.sampleapp.view.animation.ViewHeightAnimator;
import com.mapquest.navigation.internal.unit.Duration;
import com.mapquest.navigation.model.LaneInfo;
import com.mapquest.navigation.model.LaneMarkingType;
import com.mapquest.navigation.model.Maneuver;

import java.util.List;
import java.util.Set;

import static com.mapquest.navigation.sampleapp.util.UiUtil.setMargin;

/**
 * Layout that displays the current lane guidance information. As of version 3.1 of the sdk, lane
 * guidance data is empty so this view is intentionally being hidden in the xml where it is being defined.
 */
public class LaneGuidanceBar extends LinearLayout {
    private static final int SPACING_DP = 5;
    private int mExpandedHeight;

    public LaneGuidanceBar(Context context) {
        this(context, null);
    }

    public LaneGuidanceBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LaneGuidanceBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public LaneGuidanceBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
    }

    private void initialize() {
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Save off the height to restore later, then collapse down for the time being
        ViewGroup.LayoutParams params = getLayoutParams();
        mExpandedHeight = params.height;
        params.height = 0;
//        requestLayout();
    }

    public void setLanes(Maneuver maneuver) {
        removeAllViews();

        if(maneuver == null || maneuver.getLaneInfo() == null) {
            animateHeight(0);
        } else {
            List<LaneInfo> laneInfo = maneuver.getLaneInfo();

            for (int i = 0; i < laneInfo.size(); i++) {
                Set<LaneMarkingType> markings = laneInfo.get(i).getLaneMarkings();
                Set<LaneMarkingType> recommendation = laneInfo.get(i).getLaneHighlights();

                addView(buildLaneView(markings, recommendation));
            }

            animateHeight(mExpandedHeight);
        }
    }

    private void animateHeight(int height) {
        ViewHeightAnimator.animate(this, height, Duration.seconds(0.25).toMilliseconds());
    }

    private View buildLaneView(Set<LaneMarkingType> markings, Set<LaneMarkingType> recommendation) {
        LaneTypesView view = new LaneTypesView(getContext());
        view.setLaneTypes(markings, recommendation);

        LayoutParams params = new LayoutParams(dpToPx(60), dpToPx(60));
        setMargin(params, dpToPx(SPACING_DP));

        view.setLayoutParams(params);

        return view;
    }

    private int dpToPx(int dp) {
        return UiUtil.dpToPx(getResources(), dp);
    }
}
