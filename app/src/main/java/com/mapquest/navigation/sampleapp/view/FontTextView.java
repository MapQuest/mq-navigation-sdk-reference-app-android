package com.mapquest.navigation.sampleapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.util.TypefaceManager;
import com.mapquest.navigation.sampleapp.util.TypefaceManager.TextStyle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** TextView that supports using arbitrary fonts.
 *  TODO: Replace usages with custom font support added in v26 of the support library https://developer.android.com/guide/topics/ui/look-and-feel/fonts-in-xml.html
 */
public class FontTextView extends TextView {
    private static final int DEFAULT_STYLE_VALUE = 0;

    public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        updateTypeface(context, attrs);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        updateTypeface(context, attrs);
    }

    @SuppressWarnings("ResourceType")
    private void updateTypeface(Context context, AttributeSet attributes) {
        if(!isInEditMode()) {
            TypedArray attributesArray = context.obtainStyledAttributes(attributes, asArray(
                    R.attr.fontName, android.R.attr.textStyle));

            String fontName = attributesArray.getString(R.styleable.FontTextView_fontName);
            // 1 is the index from the array passed to obtainStyledAttributes() above.
            int styleFlags = attributesArray.getInt(1, DEFAULT_STYLE_VALUE);

            attributesArray.recycle();

            setTypeface(TypefaceManager.getOrCreateTypeface(getContext().getAssets(),
                    fontName, buildStyleSetFromFlags(styleFlags)));
        }
    }

    private static Set<TextStyle> buildStyleSetFromFlags(int flags) {
        Set<TextStyle> styles = new HashSet<>();

        if((flags & 1) != 0) {
            styles.add(TextStyle.BOLD);
        }
        if((flags & 2) != 0) {
            styles.add(TextStyle.ITALIC);
        }

        return Collections.unmodifiableSet(styles);
    }

    private static int[] asArray(int... values) {
        return values;
    }
}
