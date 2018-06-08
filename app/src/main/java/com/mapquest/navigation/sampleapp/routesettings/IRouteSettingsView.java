package com.mapquest.navigation.sampleapp.routesettings;

import com.mapquest.android.commoncore.model.LatLng;
import com.mapquest.android.searchahead.model.response.SearchAheadResult;
import com.mapquest.navigation.model.RouteOptionType;
import com.mapquest.navigation.model.SystemOfMeasurement;

import java.util.List;

/**
 * Represents the contract a RouteSettingsView class needs to implement in order for it to function correctly
 */
public interface IRouteSettingsView {
    void clearSearchAheadList();
    void handleSearchAheadResults(List<SearchAheadResult> results);
    LatLng getCurrentMapCenter();

    void setHighwaysOption(RouteOptionType routeOptionType);
    void setTollwaysOption(RouteOptionType routeOptionType);
    void setFerriesOption(RouteOptionType routeOptionType);
    void setUnpavedOption(RouteOptionType routeOptionType);
    void setInternationalBordersOption(RouteOptionType routeOptionType);
    void setSeasonalClosuresOption(RouteOptionType routeOptionType);
    void setSystemOfMeasurementOption(SystemOfMeasurement systemOfMeasurement);
    void setLanguage(String languageCode);
    void setRallyMode(Boolean rallyMode);
}
