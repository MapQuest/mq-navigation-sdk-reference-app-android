package com.mapquest.navigation.sampleapp.routesettings;

public enum NavigationLanguage {
    ENGLISH_US("en_US"),
    SPANISH_US("es_US");

    NavigationLanguage(String languageCode) {
        mLanguageCode = languageCode;
    }
    public String getLanguageCode() {
        return mLanguageCode;
    }

    private String mLanguageCode;
}
