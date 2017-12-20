package com.mapquest.navigation.sampleapp.searchahead;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapquest.android.commoncore.model.LatLng;
import com.mapquest.android.searchahead.IllegalQueryParameterException;
import com.mapquest.android.searchahead.model.SearchAheadQuery;
import com.mapquest.android.searchahead.model.SearchCollection;
import com.mapquest.navigation.sampleapp.BuildConfig;
import com.mapquest.navigation.sampleapp.R;
//import com.mapquest.navigation.sampleapp.ISampleAppConfiguration;
//import com.mapquest.navigation.sampleapp.SampleAppConfiguration;
import com.mapquest.navigation.sampleapp.ISampleAppConfiguration;
import com.mapquest.navigation.sampleapp.SampleAppConfiguration;
import com.mapquest.navigation.sampleapp.searchahead.fragment.AbstractFragment;
import com.mapquest.navigation.sampleapp.searchahead.util.AddressDisplayUtil;
import com.mapquest.navigation.sampleapp.searchahead.view.OnSingleItemClickListener;
import com.mapquest.navigation.sampleapp.util.UiUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

public class SearchAheadFragment extends AbstractFragment<SearchAheadFragmentCallbacks>
        implements SearchBarViewCallbacks, AbsListView.OnScrollListener {

    public static final String EXTRA_MAP_CENTER_LAT = "map_center_lat";
    public static final String EXTRA_MAP_CENTER_LNG = "map_center_lng";
    public static final String EXTRA_SEARCH_PROCESSED_FLAG = "extra_search_in_progress";
    public static final String EXTRA_SEARCH_TEXT = "extra_search_text";

    @BindView(R.id.search_bar_view_container)
    protected FrameLayout mSearchBarViewContainer;

    @BindView(R.id.search_ahead_list_view)
    protected ListView mSearchAheadListView;

    private View mRootView;
    private SearchBarView mSearchBarView;

    private static final int SEARCH_AHEAD_RESULT_LIMIT = 10;
    private SearchAheadAdapter mSearchAheadAdapter;
    private SearchAheadPerformer mSearchAheadPerformer;
    private SearchActivityServicePerformer mSearchActivityServicePerformer;
    private SearchAheadFeedback mSearchAheadFeedback;
    private final static List<SearchCollection> SEARCH_AHEAD_SEARCH_COLLECTIONS = Arrays.asList(
            SearchCollection.ADDRESS, SearchCollection.ADMINAREA, SearchCollection.POI,
            SearchCollection.AIRPORT, SearchCollection.FRANCHISE);

    private LatLng mCurrentMapCenter;

    private SampleAppConfiguration mAceConfig;

    private Toast mMaxCharacterToast;

    private String mText;
    private boolean mSearchTextProcessed;

    public static SearchAheadFragment newInstance(LatLng mapCenter) {

        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MAP_CENTER_LAT, mapCenter.getLatitude());
        bundle.putSerializable(EXTRA_MAP_CENTER_LNG, mapCenter.getLongitude());

        SearchAheadFragment fragment = new SearchAheadFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle infoBundle = savedInstanceState != null ? savedInstanceState : getArguments();
        mSearchTextProcessed = infoBundle.getBoolean(EXTRA_SEARCH_PROCESSED_FLAG, false);

        float lat = (float) getArguments().getSerializable(EXTRA_MAP_CENTER_LAT);
        float lng = (float) getArguments().getSerializable(EXTRA_MAP_CENTER_LNG);
        mCurrentMapCenter = new LatLng(lat, lng);

        mText = "";

        mSearchAheadAdapter = new SearchAheadAdapter(getActivity());
        mSearchAheadPerformer = new SearchAheadPerformer(this
                .getActivity().getApplicationContext(), BuildConfig.API_KEY);
        mSearchActivityServicePerformer = new SearchActivityServicePerformer(
                new SearchActivityServiceClient(getConfig()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = inflater.inflate(R.layout.view_search_ahead, container, false);
        ButterKnife.bind(this, mRootView);

        mSearchBarView = new SearchBarView(getActivity(), this);
        mSearchBarViewContainer.addView(mSearchBarView);

        updateTextLineUiElements();
        mSearchAheadListView.setEmptyView(null);
        setupListeners();

        if (mText != null || mSearchTextProcessed) {
            // Set search field if coming back to a restored fragment
            mSearchBarView.setSearchField(mText);
        }

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_SEARCH_TEXT, mText);
        outState.putBoolean(EXTRA_SEARCH_PROCESSED_FLAG, mSearchTextProcessed);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        UiUtil.hideKeyboard(getView());
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Dismiss keyboard when scrolling list
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            view.requestFocus();
            UiUtil.hideKeyboard(view);
        }
    }

    /** START: SearchBarViewCallbacks **/

    @Override
    public void onCancel() {
        if (getCallbacks() != null) {
            getCallbacks().onCancel();
        }
    }

    @Override
    public void onUpdateContentForSearchText(String text) {
        updateContentForSearchText(text);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_NULL
                && (event == null || event.getAction() == KeyEvent.ACTION_DOWN)
                && StringUtils.isNotBlank(mText)) {
            UiUtil.hideKeyboard(v);
        }
        return false;
    }

    @Override
    public void onClearClicked() {
        mText = "";
    }

    /** Search Ahead UI Updates **/

    protected void updateContentForSearchText(final String searchText) {
        mText = searchText;
        mSearchTextProcessed = true;

        if (searchText.length() < 2) {
            mSearchAheadAdapter.clear();
        } else {
            SearchAheadQuery searchAheadQuery;
            try {
                Log.d(TAG, "updateContentForSearchText mText: " + mText + " ; mCurrentMapCenter: " + mCurrentMapCenter);
                searchAheadQuery = new SearchAheadQuery.Builder(mText, SEARCH_AHEAD_SEARCH_COLLECTIONS)
                        .location(mCurrentMapCenter)
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

            mSearchAheadPerformer.predictResultsFromQuery(searchAheadQuery,
                    new SearchAheadPerformer.SearchAheadResponseCallback() {
                        @Override
                        public void onSuccess(@NonNull SearchAheadResponse response) {
                            List<SearchAheadResult> searchAheadResults = response.getResults();

                            if (searchAheadResults != null) {
                                mSearchAheadAdapter.clear();
                                mSearchAheadAdapter.addAll(searchAheadResults);
                            }

                            mSearchAheadFeedback = response.getFeedback();
                            if (mSearchAheadFeedback != null) {
                                mSearchActivityServicePerformer.makeViewedSearchActivityRequest(mSearchAheadFeedback,
                                        response.getResults());
                            }
                        }
                    });
        }
    }

    @NonNull
    public ISampleAppConfiguration getConfig() {
        if (mAceConfig == null) {
            mAceConfig = new SampleAppConfiguration(getContext());
        }
        return mAceConfig;
    }

    protected void updateTextLineUiElements() {
        mSearchBarView.updateTextLineUiElements();
    }

    protected void setupListeners() {
        setUpListView(mSearchAheadListView, mSearchAheadAdapter, false);
    }

    protected void setUpListView(final ListView listView, ListAdapter adapter, boolean resetScrollPosition) {
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(getOnSingleItemClickListener());
        listView.setAdapter(adapter);
        if (resetScrollPosition) {
            // Don't remember list scroll offset. Romain Guy describes a workaround
            // but gives no explanation as to why it's necessary:
            // https://groups.google.com/d/msg/android-developers/EnyldBQDUwE/BhWOgBtv2ycJ
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPosition(0);
                }
            });
        }
    }

    protected OnSingleItemClickListener getOnSingleItemClickListener() {
        return new OnSingleItemClickListener() {
            @Override
            public void onSingleItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchAheadResult searchAheadResult = getSelectedResultModel(parent, position);

                setTextWithoutSearching(
                        AddressDisplayUtil.forResources(getResources()).getDisplayString(searchAheadResult));

                updateTextLineUiElements();

                UiUtil.hideKeyboard(view);

                if (getCallbacks() != null) {
                    getCallbacks().onSelectResult(searchAheadResult);
                }
            }
        };
    }

    protected SearchAheadResult getSelectedResultModel(AdapterView<?> parent, int selectedPosition) {
        return (SearchAheadResult) parent.getItemAtPosition(selectedPosition);
    }

    protected void setTextWithoutSearching(String text) {
        mText = text;
        mSearchBarView.setTextWithoutSearching(text);
    }

    public void setSearchFieldNoQuery(String text) {
        if (isAdded()) {
            mSearchBarView.setTextWithoutSearching(text);
            mText = text;
            updateTextLineUiElements();
        }
    }
}
