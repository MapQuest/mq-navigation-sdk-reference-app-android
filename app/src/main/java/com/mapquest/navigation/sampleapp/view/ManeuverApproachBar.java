package com.mapquest.navigation.sampleapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.view.animation.ViewWeightAnimator;
import com.mapquest.navigation.internal.collection.CollectionsUtil;
import com.mapquest.navigation.model.Maneuver;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mapquest.navigation.sampleapp.util.ColorUtil.hsvToColor;
import static com.mapquest.navigation.internal.collection.CollectionsUtil.asArray;
import static com.mapquest.navigation.internal.util.Normalization.clamp;

/**
 * A progress bar that displays both the maneuver type icon and the distance to the next maneuver.
 * The maneuver types supported are listed in the LEFT_ORIENTATION_MANEUVER_TYPES set and the
 * RIGHT_ORIENTATION_MANEUVER_TYPES set but the only icons that are shown are a left-turn or a
 * right-turn maneuver based on what direction the maneuver is going e.g. A SLIGHT_LEFT maneuver will
 * display the same left-turn icon as LEFT maneuver.
 */
public class ManeuverApproachBar extends LinearLayout {
    private static final double MAX_MANEUVER_DISTANCE_IN_METERS = 400; // ~1/4 mile

    private static final Set<Maneuver.Type> LEFT_ORIENTATION_MANEUVER_TYPES = CollectionsUtil.asSet(
            Maneuver.Type.LEFT_FORK, Maneuver.Type.SLIGHT_LEFT, Maneuver.Type.LEFT,
            Maneuver.Type.SHARP_LEFT, Maneuver.Type.LEFT_UTURN);
    private static final Set<Maneuver.Type> RIGHT_ORIENTATION_MANEUVER_TYPES = CollectionsUtil.asSet(
            Maneuver.Type.RIGHT_FORK, Maneuver.Type.SLIGHT_RIGHT, Maneuver.Type.RIGHT,
            Maneuver.Type.SHARP_RIGHT, Maneuver.Type.RIGHT_UTURN);

    @BindView(R.id.maneuver_indicator)
    protected View mManeuverIndicator;
    @BindView(R.id.bar_background)
    protected LinearLayout mBarBackground;
    @BindView(R.id.bar)
    protected View mBar;

    private Orientation mOrientation;
    private Set<Maneuver.Type> mTargetManeuverTypes;
    private Maneuver.Type mCurrentManeuverType;

    public ManeuverApproachBar(Context context) {
        this(context, null);
    }

    public ManeuverApproachBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ManeuverApproachBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initializeView(context, attrs);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public ManeuverApproachBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initializeView(context, attrs);
    }

    private void initializeView(Context context, AttributeSet attributes) {
        Orientation orientation = extractOrientation(context, attributes);
        mOrientation = orientation;

        mTargetManeuverTypes = orientation == Orientation.LEFT ?
                LEFT_ORIENTATION_MANEUVER_TYPES :
                RIGHT_ORIENTATION_MANEUVER_TYPES;

        int layoutResourceId = orientation == Orientation.LEFT ?
                R.layout.maneuver_approach_bar_left :
                R.layout.maneuver_approach_bar_right;
        LayoutInflater.from(context).inflate(layoutResourceId, this, true);
        ButterKnife.bind(this);

        setVisibility(INVISIBLE);
    }

    public void update(Maneuver maneuver) {
        mCurrentManeuverType = maneuver == null ? null : maneuver.getType();
    }

    public void update(double distanceToManeuver) {
        update(distanceToManeuver, true);
    }

    private void update(Double distanceToManeuver, boolean animate) {
        if(mTargetManeuverTypes.contains(mCurrentManeuverType)) {
            float progress = determineBarProgress(distanceToManeuver);
            if(animate) {
                animateViewWeight(mBar, progress);
            } else {
                setViewWeight(mBar, progress);
            }

            int hue = (int) (30 + 90 * progress);
            int value = (int) (90 - 20 * progress);
            int primaryColor = hsvToColor(hue, 85, value);
            int mutedPrimaryColor = hsvToColor(hue, 33, value);

            mManeuverIndicator.setBackgroundColor(mutedPrimaryColor);
            mBar.setBackground(createGradientDrawable(primaryColor, mutedPrimaryColor));

            boolean inRange = 0 < distanceToManeuver && distanceToManeuver < MAX_MANEUVER_DISTANCE_IN_METERS;
            if(inRange) {
                setVisibility(VISIBLE);
            } else {
                setVisibility(INVISIBLE);
            }
        } else {
            setVisibility(INVISIBLE);
        }
    }

    private Drawable createGradientDrawable(int leftColor, int rightColor) {
        return new GradientDrawable(
                mOrientation == Orientation.LEFT ? GradientDrawable.Orientation.LEFT_RIGHT : GradientDrawable.Orientation.RIGHT_LEFT,
                asArray(leftColor, rightColor));
    }

    private float determineBarProgress(double distanceToManeuver) {
        double clampedDistance = clamp(distanceToManeuver, 0, MAX_MANEUVER_DISTANCE_IN_METERS);

        return (float) Math.sin(0.5 * Math.PI * (clampedDistance / MAX_MANEUVER_DISTANCE_IN_METERS));
    }

    public static Orientation extractOrientation(Context context, AttributeSet attributes) {
        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.ManeuverApproachBar,
                0, 0);

        try {
            return styledAttributes.getInteger(R.styleable.ManeuverApproachBar_orientation, 0) == 0 ?
                    Orientation.LEFT :
                    Orientation.RIGHT;
        } finally {
            styledAttributes.recycle();
        }
    }

    private static void animateViewWeight(View view, float weight) {
        ViewWeightAnimator.animate(view, weight, 950);
    }

    private static void setViewWeight(View view, float weight) {
        getLinearLayoutParams(view).weight = weight;
        view.getParent().requestLayout();
    }

    private static LayoutParams getLinearLayoutParams(View view) {
        return (LayoutParams) view.getLayoutParams();
    }

    private enum Orientation {
        LEFT,
        RIGHT
    }
}
