package com.mapquest.navigation.sampleapp.searchahead;

import com.mapquest.navigation.sampleapp.searchahead.fragment.FragmentCallbacks;

public interface SearchAheadFragmentCallbacks extends FragmentCallbacks {

    void onSelectResult(SearchAheadResult result);

    void onCancel();
}
