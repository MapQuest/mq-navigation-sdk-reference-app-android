package com.mapquest.navigation.sampleapp.searchahead.util;

import android.content.Context;

import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.searchahead.view.FontProvider;

public class MQFontProviderUtil {
    public static void init(Context context) {
        new FontProvider.Builder(context)
                .addFont(FontProvider.FontType.REGULAR, R.string.asset_font_filename_regular)
                .addFont(FontProvider.FontType.SEMIBOLD, R.string.asset_font_filename_semibold)
                .addFont(FontProvider.FontType.BOLD, R.string.asset_font_filename_bold)
                .addFont(FontProvider.FontType.SYMBOL, R.string.asset_font_filename_mapquest_icons)
                .addFont(FontProvider.FontType.WEATHER, R.string.asset_font_filename_mapquest_weather_icons)
                .addFont(FontProvider.FontType.LOGO, R.string.asset_font_filename_flama_medium)
                .buildInstance();
    }

    private MQFontProviderUtil() {}
}