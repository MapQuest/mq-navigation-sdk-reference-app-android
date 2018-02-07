package com.mapquest.navigation.sampleapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.util.UiUtil;
import com.mapquest.navigation.internal.util.LogUtil;
import com.mapquest.navigation.model.LaneMarkingType;

import java.util.Set;

import static com.mapquest.navigation.sampleapp.util.UiUtil.setBoundsToIntrinsicDimensions;

public class LaneTypesView extends ImageView {
    private static final int STRAIGHT_FLAG = 1;
    private static final int SLIGHT_RIGHT_TURN_FLAG = 1 << 1;
    private static final int RIGHT_TURN_FLAG = 1 << 2;
    private static final int SHARP_RIGHT_TURN_FLAG = 1 << 3;
    private static final int RIGHT_U_TURN_FLAG = 1 << 4;
    private static final int SLIGHT_LEFT_TURN_FLAG = 1 << 5;
    private static final int LEFT_TURN_FLAG = 1 << 6;
    private static final int SHARP_LEFT_TURN_FLAG = 1 << 7;
    private static final int LEFT_U_TURN_FLAG = 1 << 8;
    private static final String LOGGING_TAG = LogUtil.generateLoggingTag(LaneTypesView.class);

    public LaneTypesView(Context context) {
        this(context, null);
    }

    public LaneTypesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LaneTypesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public LaneTypesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
    }

    private void initialize() {
        setScaleType(ScaleType.FIT_CENTER);
    }

    public void setLaneTypes(Set<LaneMarkingType> types, Set<LaneMarkingType> highlightedTypes) {
        Integer typeFlags = generateFlagsForLaneTypes(types);
        if(typeFlags == null) {
            setImageResource(R.drawable.lm_0);
        } else {
            Integer highlightedTypeFlags = generateFlagsForLaneTypes(highlightedTypes);

            String drawableName = highlightedTypeFlags == null ?
                    "lm_" + typeFlags :
                    "lm_" + typeFlags + "_" + highlightedTypeFlags;

            Integer resourceId = UiUtil.getDrawableResourceId(getContext(), drawableName);
            if(resourceId == null) {
                Log.w(LOGGING_TAG, "No lane drawable for " + types + " (" + typeFlags + ") with highlight " + highlightedTypes + " (" + highlightedTypeFlags + ")");
                resourceId = R.drawable.square;
            }

            Drawable drawable = UiUtil.getDrawable(getContext(), resourceId);
            setBoundsToIntrinsicDimensions(drawable, 0.5f, 0.5f);
            setImageDrawable(drawable);
        }
    }

    private static Integer generateFlagsForLaneTypes(Set<LaneMarkingType> laneMarkingTypes) {
        if(laneMarkingTypes.isEmpty()) {
            return null;
        }

        int flags = 0;

        if(laneMarkingTypes.contains(LaneMarkingType.STRAIGHT)) {
            flags |= STRAIGHT_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.SLIGHT_RIGHT)) {
            flags |= SLIGHT_RIGHT_TURN_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.RIGHT)) {
            flags |= RIGHT_TURN_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.SHARP_RIGHT)) {
            flags |= SHARP_RIGHT_TURN_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.LEFT_U_TURN)) {
            flags |= LEFT_U_TURN_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.SHARP_LEFT)) {
            flags |= SHARP_LEFT_TURN_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.LEFT)) {
            flags |= LEFT_TURN_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.SLIGHT_LEFT)) {
            flags |= SLIGHT_LEFT_TURN_FLAG;
        }
        if(laneMarkingTypes.contains(LaneMarkingType.RIGHT_U_TURN)) {
            flags |= RIGHT_U_TURN_FLAG;
        }

        return flags;
    }
}
