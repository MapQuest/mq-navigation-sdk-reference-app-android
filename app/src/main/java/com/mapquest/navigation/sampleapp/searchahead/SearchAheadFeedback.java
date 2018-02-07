package com.mapquest.navigation.sampleapp.searchahead;

import android.support.annotation.NonNull;

import com.mapquest.android.commoncore.util.ParamUtil;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class SearchAheadFeedback {
    private final String mResultClickedUrlTemplate;
    private final String mResultViewedUrlTemplate;
    @NonNull
    private final List<SearchAheadResult> mSearchResults;

    public SearchAheadFeedback(String resultClickedUrlTemplate, String resultViewedUrlTemplate,
            @NonNull List<SearchAheadResult> results) {
        ParamUtil.validateParamsNotNull(resultClickedUrlTemplate, resultViewedUrlTemplate, results);
        this.mResultClickedUrlTemplate = resultClickedUrlTemplate;
        this.mResultViewedUrlTemplate = resultViewedUrlTemplate;
        this.mSearchResults = results;
    }

    @NonNull
    public String getResultClickedUrlTemplate() {
        return this.mResultClickedUrlTemplate;
    }

    @NonNull
    public String getResultViewedUrlTemplate() {
        return this.mResultViewedUrlTemplate;
    }

    @NonNull
    public List<SearchAheadResult> getSearchResults() {
        return mSearchResults;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
