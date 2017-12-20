package com.mapquest.navigation.sampleapp.searchahead.view;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

public final class FontProvider {
    public enum FontType {
        REGULAR, SEMIBOLD, BOLD, LOGO, SYMBOL, WEATHER
    }

    private static FontProvider INSTANCE;

    private Map<FontType, Typeface> mFonts = new HashMap<>();

    public static FontProvider get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("You must initialize the instance with the builder!");
        }
        return INSTANCE;
    }

    public static class Builder {
        private final Context mContext;
        private final Map<FontType, Typeface> mFonts = new HashMap<>();

        public Builder(Context context) {
            mContext = context;
        }

        public Builder addFont(FontType fontType, int assetFilenameResourceId) {
            return addFont(fontType, mContext.getString(assetFilenameResourceId));
        }

        public Builder addFont(FontType fontType, String assetFilename) {
            mFonts.put(fontType, Typeface.createFromAsset(mContext.getAssets(), assetFilename));
            return this;
        }

        public Map<FontType, Typeface> fonts() {
            return mFonts;
        }

        public FontProvider buildInstance() {
            INSTANCE = new FontProvider(this);
            return INSTANCE;
        }
    }

    private FontProvider(Builder builder) {
        mFonts = builder.fonts();
    }

    public Typeface getFont(FontType fontType) {
        return mFonts.get(fontType);
    }
}
