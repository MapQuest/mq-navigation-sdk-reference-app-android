package com.mapquest.navigation.sampleapp.searchahead;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mapquest.android.commoncore.util.ParamUtil;
import com.mapquest.android.searchahead.SearchAheadService;
import com.mapquest.android.searchahead.model.SearchAheadQuery;

/**
 * This class takes the new search ahead model objects and converts them into our existing models. At some point we
 * should revisit our existing interactions with the search ahead service and decouple as many of those components as
 * possible at which point we can remove this class and interface with the SearchAheadService class directly.
 */
public class SearchAheadPerformer {
    private final SearchAheadV3ResponseConverter mSearchAheadV3ResponseConverter;
    private final SearchAheadService mSearchAheadService;

    private final static String SEARCH_AHEAD_URI_STAGE = "https://com.mapquest.navigation.sampleapp.searchahead-public-api-b2c-stage.cloud.mapquest.com/search/v3/prediction";
    private final static String SEARCH_AHEAD_URI_INTEGRATION = "http://com.mapquest.navigation.sampleapp.searchahead-public-api-b2c-integration.cloud.mapquest.com/search/v3/prediction";
    private final static String SEARCH_AHEAD_URI_PROD = "https://com.mapquest.navigation.sampleapp.searchahead-public-api-b2c-production.cloud.mapquest.com/search/v3/prediction";

    public interface SearchAheadResponseCallback {
        void onSuccess(@NonNull SearchAheadResponse searchAheadResponse);
    }

    public SearchAheadPerformer(@NonNull Context context, @NonNull String apiKey) {
        this(new SearchAheadService(context, apiKey, SEARCH_AHEAD_URI_PROD),
                SearchAheadV3ResponseConverter.getInstance());
    }

    SearchAheadPerformer(SearchAheadService searchAheadService,
            SearchAheadV3ResponseConverter searchAheadV3ResponseConverter) {
        mSearchAheadService = searchAheadService;
        mSearchAheadV3ResponseConverter = searchAheadV3ResponseConverter;
    }

    public void predictResultsFromQuery(@NonNull SearchAheadQuery searchAheadQuery,
            @NonNull
            final SearchAheadResponseCallback searchAheadResponseCallback) {
        ParamUtil.validateParamsNotNull(searchAheadQuery, searchAheadResponseCallback);

        mSearchAheadService.predictResultsFromQuery(searchAheadQuery,
                new SearchAheadService.SearchAheadResponseCallback() {
                    @Override
                    public void onSuccess(
                            @NonNull com.mapquest.android.searchahead.model.response.SearchAheadResponse searchAheadResponse) {
                        searchAheadResponseCallback.onSuccess(mSearchAheadV3ResponseConverter
                                .toSearchAheadResponse(searchAheadResponse));
                    }

                    @Override
                    public void onError(Exception e) {}
                });
    }
}
