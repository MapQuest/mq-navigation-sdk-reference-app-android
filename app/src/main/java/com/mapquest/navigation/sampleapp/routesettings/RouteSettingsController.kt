package com.mapquest.navigation.sampleapp.routesettings

import android.content.Context
import android.util.Log
import com.mapquest.android.searchahead.SearchAheadService
import com.mapquest.android.searchahead.model.SearchAheadQuery
import com.mapquest.android.searchahead.model.SearchCollection
import com.mapquest.navigation.model.RouteOptionType
import com.mapquest.navigation.model.SystemOfMeasurement
import com.mapquest.navigation.sampleapp.BuildConfig
import java.lang.Exception
import java.util.*

/**
 * Holds the business logic for the Route Settings UI.
 */
class RouteSettingsController(private val searchAheadService: SearchAheadService, private val routeSettingsStorage: RouteSettingsStorage) {

    companion object {
        val TAG = "RouteSettingsController"
        private val SEARCH_AHEAD_URI_PROD = "https://searchahead-public-api-b2c-production.cloud.mapquest.com/search/v3/prediction"
        private val SEARCH_AHEAD_RESULT_LIMIT = 10
        private val SEARCH_AHEAD_COLLECTIONS = Arrays.asList(SearchCollection.ADDRESS, SearchCollection.ADMINAREA,
                SearchCollection.POI, SearchCollection.AIRPORT)
    }

    private var routeSettingsView: IRouteSettingsView? = null

    constructor(context: Context): this(
            SearchAheadService(context.applicationContext, BuildConfig.API_KEY, SEARCH_AHEAD_URI_PROD),
            RouteSettingsStorage.getInstance(context.applicationContext)
    )

    fun searchTextUpdated(searchText: String) {
        if (searchText.length < 2) {
            routeSettingsView?.clearSearchAheadList()
        } else {
            val searchAheadQuery = SearchAheadQuery.Builder(searchText, SEARCH_AHEAD_COLLECTIONS)
                    .location(routeSettingsView?.currentMapCenter)
                    .limit(SEARCH_AHEAD_RESULT_LIMIT)
                    .feedback(true)
                    .build()

            searchAheadService.predictResultsFromQuery(searchAheadQuery, object: SearchAheadService.SearchAheadResponseCallback {
                override fun onSuccess(response: com.mapquest.android.searchahead.model.response.SearchAheadResponse) {
                    routeSettingsView?.handleSearchAheadResults(response.results)
                }

                override fun onError(exception: Exception?) {
                    //TODO: Handle error in UI
                    Log.e(TAG, "Error making search ahead request")
                }
            })
        }
    }

    fun setRouteSettingsView(routeSettingsView: IRouteSettingsView) {
        this.routeSettingsView = routeSettingsView
    }

    fun removeRouteSettingsView() {
        this.routeSettingsView = null
    }

    fun readRouteOptionsFromModel() {
        val routeOptionTypes = routeSettingsStorage.readRouteOptionTypes()

        routeOptionTypes[RouteSettingsStorage.RouteOptionKey.TOLLWAYS]?.let { routeSettingsView?.setTollwaysOption(it) }

        routeOptionTypes[RouteSettingsStorage.RouteOptionKey.HIGHWAYS]?.let { routeSettingsView?.setHighwaysOption(it) }

        routeOptionTypes[RouteSettingsStorage.RouteOptionKey.FERRIES]?.let { routeSettingsView?.setFerriesOption(it) }

        routeOptionTypes[RouteSettingsStorage.RouteOptionKey.UNPAVED]?.let { routeSettingsView?.setUnpavedOption(it) }

        routeOptionTypes[RouteSettingsStorage.RouteOptionKey.INTERNATIONAL_BORDERS]?.let { routeSettingsView?.setInternationalBordersOption(it) }

        routeOptionTypes[RouteSettingsStorage.RouteOptionKey.SEASONAL_CLOSURES]?.let { routeSettingsView?.setSeasonalClosuresOption(it) }

        routeSettingsView?.setSystemOfMeasurementOption(routeSettingsStorage.readSystemOfMeasurement())

        routeSettingsView?.setLanguage(routeSettingsStorage.readLanguageCode())

        routeSettingsView?.setRallyMode(routeSettingsStorage.readRallyMode());
    }

    fun writeRouteOptionToModel(routeOptionKey: RouteSettingsStorage.RouteOptionKey, routeOptionType: RouteOptionType) {
        routeSettingsStorage.writeRouteOptionType(routeOptionKey, routeOptionType)
    }

    fun writeSystemOfMeasurementToModel(routeOptionKey: RouteSettingsStorage.RouteOptionKey, systemOfMeasurement: SystemOfMeasurement) {
        routeSettingsStorage.writeSystemOfMeasurementType(routeOptionKey, systemOfMeasurement)
    }

    fun writeLanguageCodeToModel(routeOptionKey: RouteSettingsStorage.RouteOptionKey, languageCode: String) {
        routeSettingsStorage.writeLangugageCode(routeOptionKey, languageCode)
    }

    fun writeRallyModeToModel(routeOptionKey: RouteSettingsStorage.RouteOptionKey, rallyMode: Boolean) {
        routeSettingsStorage.writeRallyMode(routeOptionKey, rallyMode);
    }
}