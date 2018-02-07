package com.mapquest.navigation.sampleapp.searchahead.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mapquest.navigation.sampleapp.R;

/**
 * {@link TextView} whose typeface can be set in layout XML using the aceTypeface attribute.
 * Typeface options include symbol, mqLogo, semibold, regular, weather and bold.
 */
public class MQTextView extends TextView {
    public static final int SYSTEM = 0;
    public static final int SYMBOL = 1;
    public static final int LOGO = 2;
    public static final int SEMIBOLD = 3;
    public static final int REGULAR = 4;
    public static final int WEATHER = 5;
    public static final int BOLD = 6;

    public MQTextView(Context context) {
        this(context, null);
    }

    public MQTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public MQTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MQTextView, defStyle, 0);
        int typefaceIndex = array.getInt(R.styleable.MQTextView_MQTypeface, REGULAR);
        array.recycle();

        setTypefaceByIndex(typefaceIndex);
    }

    protected void setTypefaceByIndex(int typefaceIndex) {
        Typeface typeface = null;
        switch (typefaceIndex) {
            case SYMBOL:
                typeface = FontProvider.get().getFont(FontProvider.FontType.SYMBOL);
                break;
            case LOGO:
                typeface = FontProvider.get().getFont(FontProvider.FontType.LOGO);
                break;
            case SEMIBOLD:
                typeface = FontProvider.get().getFont(FontProvider.FontType.SEMIBOLD);
                break;
            case REGULAR:
                typeface = FontProvider.get().getFont(FontProvider.FontType.REGULAR);
                break;
            case WEATHER:
                typeface = FontProvider.get().getFont(FontProvider.FontType.WEATHER);
                break;
            case BOLD:
                typeface = FontProvider.get().getFont(FontProvider.FontType.BOLD);
                break;
        }

        if (typeface != null) {
            setTypeface(typeface);
        }
    }
}
