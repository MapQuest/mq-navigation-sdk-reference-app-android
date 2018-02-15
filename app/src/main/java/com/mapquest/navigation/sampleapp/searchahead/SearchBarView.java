package com.mapquest.navigation.sampleapp.searchahead;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.util.UiUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Provides the search bar for search ahead. Adapted from ACE codebase.
 */
public class SearchBarView extends LinearLayout implements TextWatcher, TextView.OnEditorActionListener {

    public static final int QUERY_STRING_SIZE_MAX = 100;

    @BindView(R.id.text_entry)
    protected AppCompatEditText mTextEdit;

    @BindView(R.id.clear_button)
    protected AppCompatImageButton mClearButton;

    @OnClick(R.id.clear_button)
    protected void onClearClick() {
        mTextEdit.getText().clear();
    }

    @Nullable
    private SearchBarViewCallbacks mSearchBarViewCallbacks;
    @Nullable
    private String mText;

    public SearchBarView(Context context) {
        this(context, (AttributeSet) null);
    }

    public SearchBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_search_bar, this, true);

        ButterKnife.bind(view, this);

        boolean isPlaceHolder = attrs != null && getContext().getTheme()
                .obtainStyledAttributes(attrs, R.styleable.SearchBarView, 0, 0)
                .getBoolean(R.styleable.SearchBarView_placeHolder, false);

        // If the view is meant to be a place holder the view should just handle clicks at the root level
        if (isPlaceHolder) {
            mTextEdit.setClickable(false);
            mTextEdit.setFocusable(false);
            mTextEdit.setMovementMethod(null);
            mTextEdit.setKeyListener(null);
        }

        mText = "";

        // Only allow one extra character more than max allowed within the edit text field.
        mTextEdit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(QUERY_STRING_SIZE_MAX + 1) });
        mTextEdit.setText(mText);
        mTextEdit.setHint(getContext().getString(R.string.enter_location));

        mTextEdit.addTextChangedListener(this);
        mTextEdit.setOnEditorActionListener(this);

        view.setBackgroundColor(getResources().getColor(R.color.white));
    }

    public void setSearchBarViewCallbacks(SearchBarViewCallbacks searchBarViewCallbacks) {
        mSearchBarViewCallbacks = searchBarViewCallbacks;
    }

    public void setFocusOnEditText() {
        UiUtil.showKeyboard(mTextEdit);
    }

    public void clearSearchField() {
        mTextEdit.setText("");
    }

    public String getSearchText() {
        return mTextEdit.getText().toString();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return mSearchBarViewCallbacks != null && mSearchBarViewCallbacks.onEditorAction(v, actionId, event);
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence newText, int replacementStart,
            int replacedLength, int replacementLength) {
        mText = newText.toString().trim();

        updateTextLineUiElements();
        if (mSearchBarViewCallbacks != null) {
            mSearchBarViewCallbacks.onUpdateContentForSearchText(mText);
        }
    }

    private void updateClearButton() {
        mClearButton.setVisibility(mText.length() > 0 ? View.VISIBLE : View.GONE);
    }

    private void updateTextLineUiElements() {
        updateClearButton();
    }
}
