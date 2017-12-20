
package com.mapquest.navigation.sampleapp.view;

import android.content.Context;
import android.view.View;

import com.mapquest.navigation.sampleapp.util.DrawableMappingUtil;
import com.mapquest.navigation.model.Instruction;
import com.mapquest.navigation.model.Maneuver;

import java.util.Map;

/**
 * Manages the route's directions list data
 */
public class DirectionsListAdapter extends NarrativeAdapter<Instruction> {

    private static final Map<Maneuver.Type, Integer> MANEUVER_DRAWABLE_IDS_BY_TYPE = DrawableMappingUtil.buildManeuverDrawableIdMapping();

    public DirectionsListAdapter(Context context) {
        super(context);
    }

    @Override
    protected void populate(int position, Instruction item, NarrativeViewHolder holder) {
        if (!MANEUVER_DRAWABLE_IDS_BY_TYPE.containsKey(item.getManeuverType())) {
            holder.maneuverIcon.setVisibility(View.INVISIBLE);
        } else {
            holder.maneuverIcon.setImageResource(MANEUVER_DRAWABLE_IDS_BY_TYPE.get(item.getManeuverType()));
            holder.maneuverIcon.setVisibility(View.VISIBLE);
        }

        holder.text.setText(item.getInstruction());
    }
}
