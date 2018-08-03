package com.mapquest.navigation.sampleapp.routesettings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mapquest.android.commoncore.model.LatLng;
import com.mapquest.android.searchahead.model.response.Place;
import com.mapquest.android.searchahead.model.response.SearchAheadResult;
import com.mapquest.navigation.model.RouteOptionType;
import com.mapquest.navigation.model.SystemOfMeasurement;
import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.location.CurrentLocationProvider;
import com.mapquest.navigation.sampleapp.searchahead.SearchAheadAdapter;
import com.mapquest.navigation.sampleapp.searchahead.SearchBarView;
import com.mapquest.navigation.sampleapp.searchahead.SearchBarViewCallbacks;
import com.mapquest.navigation.sampleapp.util.UiUtil;
import com.mapquest.navigation.sampleapp.view.OnSingleItemClickListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteSettingsFragment extends Fragment implements IRouteSettingsView {

    private static final String CURRENT_ROUTE_STOPS_KEY = "current_route_stops";

    @Nullable
    private CurrentLocationProvider mCurrentLocationProvider;
    @Nullable
    private RouteSettingsController mRouteSettingsController;
    @Nullable
    private SearchAheadAdapter mSearchAheadAdapter;
    @Nullable
    private RouteStopsAdapter mRouteStopsAdapter;
    @Nullable
    private List<RouteStop> mInitialRouteStops;

    @BindView(R.id.searchBarView)
    protected SearchBarView mSearchBarView;

    @BindView(R.id.searchAheadListView)
    protected ListView mSearchAheadListView;

    @BindView(R.id.destinationsRecyclerView)
    protected RecyclerView mDestinationsRecyclerView;

    @BindView(R.id.highwaysSpinner)
    protected AppCompatSpinner mHighwaysSpinner;

    @BindView(R.id.tollwaysSpinner)
    protected AppCompatSpinner mTollwaysSpinner;

    @BindView(R.id.ferriesSpinner)
    protected AppCompatSpinner mFerriesSpinner;

    @BindView(R.id.unpavedSpinner)
    protected AppCompatSpinner mUnpavedSpinner;

    @BindView(R.id.internationalBordersSpinner)
    protected AppCompatSpinner mInternationalBordersSpinner;

    @BindView(R.id.seasonalClosuresSpinner)
    protected AppCompatSpinner mSeasonalClosuresSpinner;

    @BindView(R.id.rallyModeSpinner)
    protected AppCompatSpinner mRallyModeSpinner;

    @BindView(R.id.systemOfMeasurementSpinner)
    protected AppCompatSpinner mSystemOfMeasurementSpinner;

    @BindView(R.id.navigationLanguageSpinner)
    protected AppCompatSpinner mNavigationLanguageSpinner;

    public static RouteSettingsFragment newInstance(List<RouteStop> currentRouteStops) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(CURRENT_ROUTE_STOPS_KEY, new ArrayList<>(currentRouteStops));

        RouteSettingsFragment routeSettingsFragment = new RouteSettingsFragment();
        routeSettingsFragment.setArguments(bundle);
        return routeSettingsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle infoBundle = savedInstanceState != null ? savedInstanceState : getArguments();
        mInitialRouteStops = infoBundle.getParcelableArrayList(CURRENT_ROUTE_STOPS_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.route_settings_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // initial route-stops should contain at least the current location; if not, bail...
        if (mInitialRouteStops == null) {
            getFragmentManager().popBackStack();
            return;
        }

        mRouteSettingsController = new RouteSettingsController(getContext().getApplicationContext());

        mCurrentLocationProvider = (CurrentLocationProvider) getActivity();


        mSearchAheadAdapter = new SearchAheadAdapter(getContext().getApplicationContext());
        mSearchAheadListView.setAdapter(mSearchAheadAdapter);
        mSearchAheadListView.setOnItemClickListener(new OnSingleItemClickListener() {
            @Override
            public void onSingleItemClick(AdapterView parent, View view, int position, long id) {
                SearchAheadResult searchAheadResult = (SearchAheadResult) parent.getItemAtPosition(position);

                Place resultPlace = searchAheadResult.getPlace();
                String mqId = searchAheadResult.getSearchAheadId().getMqId();
                if (resultPlace != null) {
                    LatLng latLng = searchAheadResult.getPlace().getLatLng();
                    Coordinate searchResultCoordinate = new Coordinate(latLng.getLatitude(), latLng.getLongitude());
                    RouteStop routeStop = new RouteStop(searchAheadResult.getDisplayString(), searchResultCoordinate, mqId);

                    mRouteStopsAdapter.addRouteStop(routeStop);
                    mSearchBarView.clearSearchField();
                }
            }
        });

        RouteStopsChangedListener routeStopsChangedListener = (RouteStopsChangedListener) getActivity();
        mRouteStopsAdapter = new RouteStopsAdapter(routeStopsChangedListener);
        mDestinationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mDestinationsRecyclerView.setAdapter(mRouteStopsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL);
        mDestinationsRecyclerView.addItemDecoration(dividerItemDecoration);

        mRouteStopsAdapter.addRouteStops(mInitialRouteStops);
    }

    @Override
    public void onResume() {
        super.onResume();

        mRouteSettingsController.setRouteSettingsView(this);
        mRouteSettingsController.readRouteOptionsFromModel();

        mSearchBarView.setSearchBarViewCallbacks(new SearchBarViewCallbacks() {

            @Override
            public void onUpdateContentForSearchText(String text) {
                if (text.length() > 0) {
                    mSearchAheadListView.setVisibility(View.VISIBLE);
                } else {
                    mSearchAheadListView.setVisibility(View.GONE);
                }

                mRouteSettingsController.searchTextUpdated(text);
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

        // Set the focus on the search bar edit text when the fragment is initialized
        mSearchBarView.setFocusOnEditText();

        mHighwaysSpinner.setOnItemSelectedListener(new RouteOptionOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.HIGHWAYS));
        mTollwaysSpinner.setOnItemSelectedListener(new RouteOptionOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.TOLLWAYS));
        mFerriesSpinner.setOnItemSelectedListener(new RouteOptionOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.FERRIES));
        mUnpavedSpinner.setOnItemSelectedListener(new RouteOptionOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.UNPAVED));
        mInternationalBordersSpinner.setOnItemSelectedListener(new RouteOptionOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.INTERNATIONAL_BORDERS));
        mSeasonalClosuresSpinner.setOnItemSelectedListener(new RouteOptionOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.SEASONAL_CLOSURES));
        mSystemOfMeasurementSpinner.setOnItemSelectedListener(new SystemOfMeasurementOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.SYSTEM_OF_MEASUREMENT));
        mNavigationLanguageSpinner.setOnItemSelectedListener(new NavigationLanguageOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.LANGUAGE_CODE));
        mRallyModeSpinner.setOnItemSelectedListener(new RallyModeOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey.RALLY_MODE));
    }

    @Override
    public void onPause() {
        mRouteSettingsController.removeRouteSettingsView();
        super.onPause();
    }

    @Override
    public void clearSearchAheadList() {
        mSearchAheadAdapter.clear();
    }

    @Override
    public void handleSearchAheadResults(List<SearchAheadResult> results) {
        mSearchAheadAdapter.clear();
        mSearchAheadAdapter.addAll(results);
    }

    @Override
    public LatLng getCurrentMapCenter() {
        return mCurrentLocationProvider.getCurrentLocation();
    }

    @Override
    public void setHighwaysOption(RouteOptionType routeOptionType) {
        int position = getPositionFromRouteOptionType(routeOptionType);
        mHighwaysSpinner.setSelection(position);
    }

    @Override
    public void setTollwaysOption(RouteOptionType routeOptionType) {
        int position = getPositionFromRouteOptionType(routeOptionType);
        mTollwaysSpinner.setSelection(position);
    }

    @Override
    public void setFerriesOption(RouteOptionType routeOptionType) {
        int position = getPositionFromRouteOptionType(routeOptionType);
        mFerriesSpinner.setSelection(position);
    }

    @Override
    public void setUnpavedOption(RouteOptionType routeOptionType) {
        int position = getPositionFromRouteOptionType(routeOptionType);
        mUnpavedSpinner.setSelection(position);
    }

    @Override
    public void setInternationalBordersOption(RouteOptionType routeOptionType) {
        int position = getPositionFromRouteOptionType(routeOptionType);
        mInternationalBordersSpinner.setSelection(position);
    }

    @Override
    public void setSeasonalClosuresOption(RouteOptionType routeOptionType) {
        int position = getPositionFromRouteOptionType(routeOptionType);
        mSeasonalClosuresSpinner.setSelection(position);
    }

    @Override
    public void setRallyMode(Boolean rallyMode) {
        int position = getPositionFromRallyMode(rallyMode);
        mRallyModeSpinner.setSelection(position);
    }

    @Override
    public void setSystemOfMeasurementOption(SystemOfMeasurement systemOfMeasurement) {
        int position = systemOfMeasurement.ordinal();
        mSystemOfMeasurementSpinner.setSelection(position);
    }

    @Override
    public void setLanguage(String languageCode) {
        int position = getPositionFromLanguageCode(languageCode);
        mNavigationLanguageSpinner.setSelection(position);
    }

    private int getPositionFromRouteOptionType(RouteOptionType routeOptionType) {
        switch (routeOptionType) {
            case ALLOW:
                return 0;
            case DISALLOW:
                return 1;
            case AVOID:
                return 2;
            default:
                return 0;
        }
    }

    private int getPositionFromLanguageCode(String languageCode) {
        if (languageCode.equals(NavigationLanguage.ENGLISH_US.getLanguageCode())) {
            return 0;
        } else if (languageCode.equals(NavigationLanguage.SPANISH_US.getLanguageCode())) {
            return 1;
        } else {
            return 0;
        }
    }

    private int getPositionFromRallyMode(Boolean rallyMode) {
        return rallyMode ? 0 : 1;
    }

    private class RouteOptionOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private final RouteSettingsStorage.RouteOptionKey mRouteOptionKey;

        RouteOptionOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey routeOptionKey) {
            mRouteOptionKey = routeOptionKey;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (mRouteSettingsController != null) {
                mRouteSettingsController.writeRouteOptionToModel(mRouteOptionKey, getRouteOptionType(i));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}

        private RouteOptionType getRouteOptionType(int index) {
            switch (index) {
                case 0:
                    return RouteOptionType.ALLOW;
                case 1:
                    return RouteOptionType.DISALLOW;
                case 2:
                    return RouteOptionType.AVOID;
                default:
                    return RouteOptionType.ALLOW;
            }
        }
    }

    private class SystemOfMeasurementOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private final RouteSettingsStorage.RouteOptionKey mRouteOptionKey;

        SystemOfMeasurementOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey routeOptionKey) {
            mRouteOptionKey = routeOptionKey;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (mRouteSettingsController != null) {
                mRouteSettingsController.writeSystemOfMeasurementToModel(mRouteOptionKey, getSystemOfMeasurement(i));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}

        private SystemOfMeasurement getSystemOfMeasurement(int index) {
            switch (index) {
                case 0:
                    return SystemOfMeasurement.UNITED_STATES_CUSTOMARY;
                case 1:
                    return SystemOfMeasurement.METRIC;
                default:
                    return SystemOfMeasurement.UNITED_STATES_CUSTOMARY;
            }
        }
    }

    private class NavigationLanguageOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private final RouteSettingsStorage.RouteOptionKey mRouteOptionKey;

        NavigationLanguageOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey routeOptionKey) {
            mRouteOptionKey = routeOptionKey;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (mRouteSettingsController != null) {
                mRouteSettingsController.writeLanguageCodeToModel(mRouteOptionKey, getLanguageCode(i));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}

        private String getLanguageCode(int index) {
            switch (index) {
                case 0:
                    return NavigationLanguage.ENGLISH_US.getLanguageCode();
                case 1:
                    return NavigationLanguage.SPANISH_US.getLanguageCode();
                default:
                    return NavigationLanguage.ENGLISH_US.getLanguageCode();
            }
        }
    }

    private class RallyModeOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private final RouteSettingsStorage.RouteOptionKey mRouteOptionKey;

        RallyModeOnItemSelectedListener(RouteSettingsStorage.RouteOptionKey routeOptionKey) {
            mRouteOptionKey = routeOptionKey;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (mRouteSettingsController != null) {
                mRouteSettingsController.writeRallyModeToModel(mRouteOptionKey, getRallyMode(i));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}

        private Boolean getRallyMode(int index) {
            switch (index) {
                case 0:
                    return true;
                case 1:
                    return false;
                default:
                    return false;
            }
        }
    }
}
