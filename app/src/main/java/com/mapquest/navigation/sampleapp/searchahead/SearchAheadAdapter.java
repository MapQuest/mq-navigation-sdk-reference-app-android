package com.mapquest.navigation.sampleapp.searchahead;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mapquest.navigation.sampleapp.R;

import java.util.ArrayList;
import java.util.List;

public class SearchAheadAdapter extends ArrayAdapter<SearchAheadResult> {
    public SearchAheadAdapter(Context context) {
        this(context, new ArrayList<SearchAheadResult>());
    }

    public SearchAheadAdapter(Context context, List<SearchAheadResult> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchAheadResult item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_ahead_list_item, parent, false);
        }

        TextView poiIcon = (TextView) convertView.findViewById(R.id.autocomplete_category_sym);
        poiIcon.setText(R.string.sym_search);

        TextView primaryAddressText = (TextView) convertView.findViewById(R.id.autocomplete_primary_text);
        primaryAddressText.setText(item.getDisplayString());

        TextView secondaryAddressText = (TextView) convertView.findViewById(R.id.autocomplete_secondary_text);
        secondaryAddressText.setText(item.getDisplayString());

        return convertView;
    }
}
