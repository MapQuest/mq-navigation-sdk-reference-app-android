package com.mapquest.navigation.sampleapp.routesettings

import android.content.Context
import android.content.SharedPreferences
import com.mapquest.navigation.internal.util.SingletonHolder
import com.mapquest.navigation.model.RouteOptionType
import com.mapquest.navigation.model.SystemOfMeasurement

class RouteSettingsStorage private constructor(private val sharedPreferences: SharedPreferences) {

    companion object : SingletonHolder<RouteSettingsStorage, Context>(::RouteSettingsStorage) {
        private val SHARED_PREFERENCES_KEY = "route_settings_storage"
    }

    enum class RouteOptionKey {
        FERRIES,
        HIGHWAYS,
        TOLLWAYS,
        UNPAVED,
        INTERNATIONAL_BORDERS,
        SEASONAL_CLOSURES,
        RALLY_MODE,
        SYSTEM_OF_MEASUREMENT,
        LANGUAGE_CODE
    }

    private constructor(context: Context): this(context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE))

    /**
     * Read route options & their values from local storage
     */
    fun readRouteOptionTypes():Map<RouteOptionKey, RouteOptionType> {
        val map = HashMap<RouteOptionKey, RouteOptionType>()

        readRouteOption(RouteOptionKey.FERRIES, map)
        readRouteOption(RouteOptionKey.HIGHWAYS, map)
        readRouteOption(RouteOptionKey.TOLLWAYS, map)
        readRouteOption(RouteOptionKey.UNPAVED, map)
        readRouteOption(RouteOptionKey.INTERNATIONAL_BORDERS, map)
        readRouteOption(RouteOptionKey.SEASONAL_CLOSURES, map)
        return map
    }


    /**
     * Write route options & their values to local storage
     */
    fun writeRouteOptionType(routeOptionKey: RouteOptionKey, routeOptionType: RouteOptionType) {
        sharedPreferences.edit()
                .putString(routeOptionKey.name, routeOptionType.name)
                .apply()
    }

    /**
     * Read system of measurement value from local storage
     */
    fun readSystemOfMeasurement(): SystemOfMeasurement = SystemOfMeasurement.valueOf(sharedPreferences.getString(RouteOptionKey.SYSTEM_OF_MEASUREMENT.name, SystemOfMeasurement.UNITED_STATES_CUSTOMARY.name))

    /**
     * Write system of measurement to from local storage
     */
    fun writeSystemOfMeasurementType(routeOptionKey: RouteOptionKey, systemOfMeasurement: SystemOfMeasurement) {
        sharedPreferences.edit()
                .putString(routeOptionKey.name, systemOfMeasurement.name)
                .apply()
    }

    /**
     * Read language code from local storage
     */
    fun readLanguageCode(): String = sharedPreferences.getString(RouteOptionKey.LANGUAGE_CODE.name, NavigationLanguage.ENGLISH_US.getLanguageCode())

    /**
     * Write language code to local storage
     */
    fun writeLangugageCode(routeOptionKey: RouteOptionKey, languageCode: String) {
        sharedPreferences.edit()
                .putString(routeOptionKey.name, languageCode)
                .apply()
    }

    /**
     * Read rally mode from local storage
     */
    fun readRallyMode(): Boolean = sharedPreferences.getBoolean(RouteOptionKey.RALLY_MODE.name, false)

    /**
     * Write rally mode to local storage
     */
    fun writeRallyMode(routeOptionKey: RouteOptionKey, rallyMode: Boolean) {
        sharedPreferences.edit()
                .putBoolean(routeOptionKey.name, rallyMode)
                .apply()
    }

    private fun readRouteOption(routeOptionKey: RouteOptionKey, map: HashMap<RouteOptionKey, RouteOptionType>) {
        map.put(routeOptionKey, readRouteOptionType(routeOptionKey))
    }

    private fun readRouteOptionType(routeOptionKey: RouteOptionKey):RouteOptionType = RouteOptionType.valueOf(sharedPreferences.getString(routeOptionKey.name, RouteOptionType.ALLOW.name))
}
