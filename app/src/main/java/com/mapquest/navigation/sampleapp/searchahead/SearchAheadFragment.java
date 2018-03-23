package com.mapquest.navigation.sampleapp.searchahead;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapquest.android.commoncore.model.LatLng;
import com.mapquest.android.searchahead.IllegalQueryParameterException;
import com.mapquest.android.searchahead.SearchAheadService;
import com.mapquest.android.searchahead.model.SearchAheadQuery;
import com.mapquest.android.searchahead.model.SearchCollection;
import com.mapquest.android.searchahead.model.response.Place;
import com.mapquest.android.searchahead.model.response.SearchAheadResponse;
import com.mapquest.android.searchahead.model.response.SearchAheadResult;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.sampleapp.BuildConfig;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.location.CurrentLocationProvider;
import com.mapquest.navigation.sampleapp.util.UiUtil;
import com.mapquest.navigation.sampleapp.view.OnSingleItemClickListener;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchAheadFragment extends Fragment {

    // NOTE: container Activity must implement this interface
    public interface OnSearchResultSelectedListener {
        public void onSearchResultSelected(String displayName, Coordinate coordinate, String mqId);
    }

    @Nullable
    private OnSearchResultSelectedListener mOnSearchResultSelectedListener;

    @Nullable
    private CurrentLocationProvider mCurrentLocationProvider;

    @Nullable
    private SearchAheadAdapter mSearchAheadAdapter;

    private static final String CURRENT_ROUTE_STOPS_KEY = "current_route_stops";

    @BindView(R.id.searchBarView)
    protected SearchBarView mSearchBarView;

    @BindView(R.id.searchAheadListView)
    protected ListView mSearchAheadListView;

    private SearchAheadService mSearchAheadService;

    private final static List<SearchCollection> SEARCH_AHEAD_SEARCH_COLLECTIONS = Arrays.asList(
            SearchCollection.ADDRESS, SearchCollection.ADMINAREA, SearchCollection.POI,
            SearchCollection.AIRPORT, SearchCollection.FRANCHISE);

    private String mText;
    private Toast mMaxCharacterToast;

    // FIXME: unused??
    private final String SEARCH_AHEAD_URI_PROD = "https://searchahead-public-api-b2c-production.cloud.mapquest.com/search/v3/prediction";

    private final static int SEARCH_AHEAD_RESULT_LIMIT = 10;

    public static SearchAheadFragment newInstance() { // List<RouteStop> currentRouteStops) {
        Bundle bundle = new Bundle();
        // add any args, if any, to the bundle here...

        SearchAheadFragment fragment = new SearchAheadFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
        // parse any saved-state from the bundle here...

        mSearchAheadService = new SearchAheadService(getActivity().getApplicationContext(), BuildConfig.API_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_ahead_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                mOnSearchResultSelectedListener = (OnSearchResultSelectedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnSearchResultSelectedListener");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mCurrentLocationProvider = (CurrentLocationProvider) getActivity();

        mSearchAheadAdapter = new SearchAheadAdapter(getActivity().getApplicationContext());
        mSearchAheadListView.setAdapter(mSearchAheadAdapter);
        mSearchAheadListView.setOnItemClickListener(new OnSingleItemClickListener() {
            @Override
            public void onSingleItemClick(AdapterView parent, View view, int position, long id) {
                SearchAheadResult searchAheadResult = (SearchAheadResult) parent.getItemAtPosition(position);

                Place resultPlace = searchAheadResult.getPlace();
                if (resultPlace != null) {
                    LatLng latLng = searchAheadResult.getPlace().getLatLng();
                    Coordinate searchResultCoordinate = new Coordinate(latLng.getLatitude(), latLng.getLongitude());
                    String mqId = searchAheadResult.getSearchAheadId().getMqId();

                    mOnSearchResultSelectedListener.onSearchResultSelected(searchAheadResult.getDisplayString(), searchResultCoordinate, mqId);
                    mSearchBarView.clearSearchField();

                    UiUtil.hideKeyboard(parent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mSearchBarView.setSearchBarViewCallbacks(new SearchBarViewCallbacks() {
            @Override
            public void onUpdateContentForSearchText(String text) {
                if (text.length() > 0) {
                    mSearchAheadListView.setVisibility(View.VISIBLE);
                } else {
                    mSearchAheadListView.setVisibility(View.GONE);
                }
                updateContentForSearchText(text);
            }

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && (event == null || event.getAction() == KeyEvent.ACTION_DOWN)
                        && StringUtils.isNotBlank(mSearchBarView.getSearchText())) {
                    UiUtil.hideKeyboard(v);
                }
                return false;
            }
        });

        // set the focus on the search bar edit text when the fragment is initialized
        mSearchBarView.setFocusOnEditText();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void updateContentForSearchText(final String searchText) {
        mText = searchText;
        LatLng currentMapCenter = ((CurrentLocationProvider) getActivity()).getCurrentLocation();

        // first, build a search-ahead query...
        if (searchText.length() < 2) {
            mSearchAheadAdapter.clear();
        } else {
            SearchAheadQuery searchAheadQuery;
            try {
                // Log.d(TAG, "updateContentForSearchText mText: " + mText + " ; mCurrentMapCenter: " + mCurrentMapCenter);
                searchAheadQuery = new SearchAheadQuery.Builder(mText, SEARCH_AHEAD_SEARCH_COLLECTIONS)
                        .location(currentMapCenter)
                        .limit(SEARCH_AHEAD_RESULT_LIMIT)
                        .feedback(true)
                        .build();

            } catch (IllegalQueryParameterException e) {
                if (searchText.length() >= SearchAheadQuery.QUERY_STRING_SIZE_MAX) {
                    if (mMaxCharacterToast != null) {
                        mMaxCharacterToast.cancel();
                    }
                    mMaxCharacterToast = Toast.makeText(getActivity(), R.string.max_search_character_limit,
                            Toast.LENGTH_SHORT);
                    mMaxCharacterToast.show();
                }
                return;
            }

            // and execute the search-ahead query...
            mSearchAheadService.predictResultsFromQuery(searchAheadQuery,
                    new SearchAheadService.SearchAheadResponseCallback() {
                        @Override
                        public void onSuccess(@NonNull SearchAheadResponse response) {
                            List<SearchAheadResult> searchAheadResults = response.getResults();

                            if (searchAheadResults != null) {
                                mSearchAheadAdapter.clear();
                                mSearchAheadAdapter.addAll(searchAheadResults);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            // FIXME: report error to listener/UI
                        }
                    });
        }
    }
}
