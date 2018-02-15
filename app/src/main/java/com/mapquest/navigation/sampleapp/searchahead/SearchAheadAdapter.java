package com.mapquest.navigation.sampleapp.searchahead;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mapquest.navigation.sampleapp.R;

import java.util.ArrayList;

public class SearchAheadAdapter extends ArrayAdapter<com.mapquest.android.searchahead.model.response.SearchAheadResult> {

    public SearchAheadAdapter(@NonNull Context context) {
        super(context, 0, new ArrayList<com.mapquest.android.searchahead.model.response.SearchAheadResult>());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        com.mapquest.android.searchahead.model.response.SearchAheadResult item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_ahead_list_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.autocomplete_primary_text))
                .setText(item.getDisplayString());

        ((TextView) convertView.findViewById(R.id.autocomplete_secondary_text))
                .setText(item.getDisplayString());

        return convertView;
    }
}
