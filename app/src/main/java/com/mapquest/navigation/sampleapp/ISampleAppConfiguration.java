package com.mapquest.navigation.sampleapp;

public interface ISampleAppConfiguration {

    /** Gets an ID that's unique for each app install (may be reset when user clears data) */
    String getPersistentInstallId();

}
