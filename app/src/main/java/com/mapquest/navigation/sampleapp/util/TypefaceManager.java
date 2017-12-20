package com.mapquest.navigation.sampleapp.util;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TypefaceManager {
    private static final String DEFAULT_FONT_NAME = "raleway";
    private static final String DEFAULT_STYLE_STRING = "regular";

    private static Map<String,Typeface> mTypefacesByFilename = new HashMap<>();

    public static synchronized Typeface getOrCreateTypeface(AssetManager assetManager, String fontName, Set<TextStyle> styles) {
        String fontPath = String.format(Locale.ROOT, "fonts/%s-%s.ttf",
                fontName == null ? DEFAULT_FONT_NAME : fontName,
                buildStyleString(styles));

        if(mTypefacesByFilename.containsKey(fontPath)) {
            return mTypefacesByFilename.get(fontPath);
        } else {
            Typeface typeface = Typeface.createFromAsset(assetManager, fontPath);
            mTypefacesByFilename.put(fontPath, typeface);
            return typeface;
        }
    }

    private static String buildStyleString(Set<TextStyle> styles) {
        if(styles.isEmpty()) {
            return DEFAULT_STYLE_STRING;
        }

        StringBuilder builder = new StringBuilder();

        if(styles.contains(TextStyle.BOLD)) {
            builder.append("bold");
        }

        if(styles.contains(TextStyle.ITALIC)) {
            if(builder.length() > 0) {
                builder.append("-");
            }
            builder.append("italic");
        }

        return builder.toString();
    }

    public enum TextStyle {
        BOLD,
        ITALIC
    }
}
