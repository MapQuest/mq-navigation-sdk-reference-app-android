package com.mapquest.navigation.sampleapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapquest.navigation.sampleapp.R;

import java.util.List;


public abstract class NarrativeAdapter<T> extends ArrayAdapter<T> {

    @NonNull
    private final LayoutInflater mInflater;

    static class NarrativeViewHolder {
        ImageView maneuverIcon;
        TextView text;
    }

    public NarrativeAdapter(Context context) {
        super(context, R.layout.narrative_list_row);

        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<T> data) {
        clear();
        addData(data);
    }

    public void addData(List<T> data) {
        for (T t : data) {
            add(t);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NarrativeViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.narrative_list_row, parent, false);
            holder = new NarrativeViewHolder();

            holder.maneuverIcon = (ImageView) convertView.findViewById(R.id.next_maneuver_icon);

            holder.text = (TextView) convertView.findViewById(R.id.instruction_text_view);
            holder.text.setTextColor(convertView.getResources().getColor(android.R.color.black));

            convertView.setTag(holder);
        } else {
            holder = (NarrativeViewHolder) convertView.getTag();
        }

        final T item = getItem(position);
        populate(position, item, holder);

        return convertView;
    }

    protected abstract void populate(int position, T item, NarrativeViewHolder holder);
}
