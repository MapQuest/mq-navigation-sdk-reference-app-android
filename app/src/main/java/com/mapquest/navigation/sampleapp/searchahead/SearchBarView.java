package com.mapquest.navigation.sampleapp.searchahead;

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapquest.android.commoncore.util.ParamUtil;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.searchahead.view.MQEditText;
import com.mapquest.navigation.sampleapp.searchahead.view.FontProvider;
import com.mapquest.navigation.sampleapp.util.UiUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Provides the search bar for search ahead. Adapted from ACE codebase.
 */
public class SearchBarView extends LinearLayout implements TextWatcher, TextView.OnEditorActionListener {

    public static final int QUERY_STRING_SIZE_MAX = 100;

    @BindView(R.id.search_ahead_list_item_icon)
    protected ImageView mTextEntryIcon;

    @BindView(R.id.text_entry)
    protected MQEditText mTextEdit;

    @BindView(R.id.clear_button)
    protected TextView mClearButton;

    @BindView(R.id.cancel_button)
    protected TextView mCancelButton;

    @OnClick(R.id.clear_button)
    protected void onClearClick() {
        mTextEdit.getText().clear();
        if (mListener != null) {
            mListener.onClearClicked();
        }
    }

    @OnClick(R.id.cancel_button)
    protected void onCancelClick() {
        UiUtil.hideKeyboard(mTextEdit);
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    private SearchBarViewCallbacks mListener;

    private String mText;

    public SearchBarView(Context context, SearchBarViewCallbacks listener) {
        super(context);
        ParamUtil.validateParamNotNull(listener);
        mListener = listener;
        initialize();
    }

    private void initialize() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_search_bar, this, true);

        ButterKnife.bind(view, this);

        setSearchIcon();

        mText = "";

        // Only allow one extra character more than max allowed within the edit text field.
        mTextEdit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(QUERY_STRING_SIZE_MAX + 1) });
        mTextEdit.setText(mText);
        mTextEdit.setHint(getContext().getString(R.string.enter_location));

        mTextEdit.addTextChangedListener(this);
        mTextEdit.setOnEditorActionListener(this);

        view.setBackgroundColor(getResources().getColor(R.color.white));

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return mListener.onEditorAction(v, actionId, event);
    }

    private void setSearchIcon() {
        Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int primaryThemeColor = getResources().getColor(R.color.black);
        iconPaint.setTypeface(FontProvider.get().getFont(FontProvider.FontType.SYMBOL));
        iconPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.search_icon_text_size));
        iconPaint.setColor(primaryThemeColor);
        int size = (int) getResources().getDimension(R.dimen.symbol_size_primary);
        mTextEntryIcon.setImageDrawable(UiUtil.convertStringToDrawable(getContext(),
                getContext().getString(R.string.sym_search), iconPaint, size, size));
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
        mListener.onUpdateContentForSearchText(mText);
    }

    private void updateClearButton() {
        mClearButton.setVisibility(mText.length() > 0 ? View.VISIBLE : View.GONE);
    }

    protected void updateTextLineUiElements() {
        updateClearButton();
    }

    public void setSearchField(String text) {
        mTextEdit.setText(text);
        mTextEdit.setSelection(text.length());
    }

    protected void setTextWithoutSearching(String text) {
        // set the text, without triggering the listeners
        mTextEdit.removeTextChangedListener(this);
        mTextEdit.setText(text);
        mTextEdit.addTextChangedListener(this);
    }
}
