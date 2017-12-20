package com.mapquest.navigation.sampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.mapquest.navigation.sampleapp.searchahead.SearchAheadConstants;

import java.util.UUID;

/**
 * Wraps the platform config and allows for extension of app-specific configs
 */
public class SampleAppConfiguration implements ISampleAppConfiguration {

    @NonNull
    private final SharedPreferences mPreferences;

    public SampleAppConfiguration(@NonNull Context context) {
        mPreferences = context.getSharedPreferences(SearchAheadConstants.SETTINGS_KEY, Context.MODE_PRIVATE);
    }

    /**
     * Get id that's unique to this install of the app. Will be reset if app data is cleared.
     */
    @Override
    public String getPersistentInstallId() {
        synchronized (mPreferences) {
            if (!mPreferences.contains(SearchAheadConstants.PERSISTENT_UNIQUE_ID_KEY)) {
                edit().putString(SearchAheadConstants.PERSISTENT_UNIQUE_ID_KEY,
                        UUID.randomUUID().toString()).commit();
            }
            return mPreferences.getString(SearchAheadConstants.PERSISTENT_UNIQUE_ID_KEY, null);
        }
    }

    @NonNull
    protected SharedPreferences getPreferences() {
        return mPreferences;
    }

    /** @return New editor instance. Do not forget to commit. */
    protected SharedPreferences.Editor edit() {
        return getPreferences().edit();
    }
}
