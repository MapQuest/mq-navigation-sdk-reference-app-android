package com.mapquest.navigation.sampleapp.searchahead;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchAheadResponse {

    private SearchAheadFeedback mFeedback;
    private List<SearchAheadResult> mResults;

    private Map<String, String> mHeaders = new HashMap<>();

    public void addResult(SearchAheadResult result) {
        if (mResults == null) {
            mResults = new ArrayList<>();
        }
        mResults.add(result);
    }

    public List<SearchAheadResult> getResults() {
        return mResults;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    @Nullable
    public void setFeedback(SearchAheadFeedback feedback) {
        mFeedback = feedback;
    }

    public SearchAheadFeedback getFeedback() {
        return mFeedback;
    }
}
