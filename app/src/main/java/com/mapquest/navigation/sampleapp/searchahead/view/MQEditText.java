package com.mapquest.navigation.sampleapp.searchahead.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class MQEditText extends EditText {
    private OnKeyPreImeListener mOnKeyPreImeListener;
    private OnTextContextMenuListener mOnTextContextMenuListener;

    public MQEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MQEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MQEditText(Context context) {
        super(context);
    }

    @Override
    public void setTypeface(Typeface tf) {
        if (!isInEditMode()) {
            super.setTypeface(FontProvider.get().getFont(FontProvider.FontType.REGULAR));
        }
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        setTypeface(null);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return mOnKeyPreImeListener != null && mOnKeyPreImeListener.onKeyPreIme(keyCode, event) ||
                super.dispatchKeyEvent(event);
    }

    public OnKeyPreImeListener getOnKeyPreImeListener() {
        return mOnKeyPreImeListener;
    }

    public void setOnKeyPreIme(OnKeyPreImeListener onKeyPreImeListener) {
        mOnKeyPreImeListener = onKeyPreImeListener;
    }

    public void setOnTextContextMenuListener(OnTextContextMenuListener listener) {
        mOnTextContextMenuListener = listener;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (mOnTextContextMenuListener != null) {
            mOnTextContextMenuListener.onContextMenuItem(id);
        }
        return super.onTextContextMenuItem(id);
    }
}