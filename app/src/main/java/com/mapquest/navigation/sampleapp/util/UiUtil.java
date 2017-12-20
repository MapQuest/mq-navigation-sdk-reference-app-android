package com.mapquest.navigation.sampleapp.util;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapquest.navigation.sampleapp.R;

public final class UiUtil {
    private static final String DRAWABLE_RESOURCE_DEF_TYPE = "drawable";

    private UiUtil() { }

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static MarkerViewOptions buildDownArrowMarkerOptions(Context context, @ColorRes int fillColorResourceId) {
        return buildDrawableMarkerOptions(context, R.drawable.marker_down, fillColorResourceId, 0.5f, 1.0f);
    }

    public static MarkerViewOptions buildUpArrowMarkerOptions(Context context, @ColorRes int fillColorResourceId) {
        return buildDrawableMarkerOptions(context, R.drawable.marker_up, fillColorResourceId, 0.5f, 0.0f);
    }

    public static MarkerViewOptions buildCircleMarkerOptions(Context context, @ColorRes int fillColorResourceId) {
        return buildDrawableMarkerOptions(context, R.drawable.circle, fillColorResourceId, 0.5f, 0.5f);
    }

    private static MarkerViewOptions buildDrawableMarkerOptions(Context context,
            @DrawableRes int drawableResourceId, @ColorRes int tintColorResourceId,
            @FloatRange(from = 0, to = 1) float centerX, @FloatRange(from = 0, to = 1) float centerY) {
        IconFactory iconFactory = IconFactory.getInstance(context);
        // Icon icon = iconFactory.defaultMarker();
        Drawable iconDrawable = ContextCompat.getDrawable(context, drawableResourceId);
        iconDrawable.setColorFilter(new PorterDuffColorFilter(getColor(context, tintColorResourceId), Mode.MULTIPLY));
        Icon icon = iconFactory.fromBitmap(drawableToBitmap(iconDrawable));

        return new MarkerViewOptions()
                .icon(icon)
                .anchor(centerX, centerY);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @TargetApi(VERSION_CODES.M)
    private static int getColor(Context context, int colorResourceId) {
        return Build.VERSION.SDK_INT >= 23 ?
                context.getResources().getColor(colorResourceId, context.getTheme()) :
                context.getResources().getColor(colorResourceId);
    }

    public static int dpToPx(Resources resources, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static void setBoundsToIntrinsicDimensions(Drawable drawable) {
        setBoundsToIntrinsicDimensions(drawable, 0, 0);
    }

    public static void setBoundsToIntrinsicDimensions(Drawable drawable, float centerX, float centerY) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        int left = (int) (-intrinsicWidth * centerX);
        int top = (int) (-intrinsicHeight * centerY);
        drawable.setBounds(left, top, intrinsicWidth + left, intrinsicHeight + top);
    }

    @SuppressWarnings("ResourceType")
    public static @DrawableRes Integer getDrawableResourceId(Context context, String drawableName) {
        int id = context.getResources().getIdentifier(
                drawableName, DRAWABLE_RESOURCE_DEF_TYPE, context.getPackageName());

        return id == 0 ? null : id;
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static Drawable getDrawable(Context context, @DrawableRes int drawableResourceId) {
        return Build.VERSION.SDK_INT >= 21 ?
            context.getResources().getDrawable(drawableResourceId, context.getTheme()) :
            context.getResources().getDrawable(drawableResourceId);
    }

    /** Sets the given margin for all sides of the given layout parameters. */
    public static void setMargin(LayoutParams params, int marginPx) {
        params.topMargin = marginPx;
        params.rightMargin = marginPx;
        params.bottomMargin = marginPx;
        params.leftMargin = marginPx;
    }

    public static ObjectAnimator newSlideBottomAnimator(View view, boolean enter, int height, int duration) {
        int d = duration != 0 ? duration : view.getResources().getInteger(
                android.R.integer.config_mediumAnimTime);
        return ObjectAnimator.ofFloat(view, "translationY", enter ? height : 0, enter ? 0 : height)
                .setDuration(d);
    }

    public static ObjectAnimator newSlideRightAnimator(View view, boolean enter, int width,
                                                       int duration) {
        int d = duration != 0 ? duration : view.getResources().getInteger(
                android.R.integer.config_mediumAnimTime);
        return ObjectAnimator.ofFloat(view, "translationX", enter ? width : 0, enter ? 0 : width)
                .setDuration(d);
    }

    public static void hideKeyboard(View view) {
        if (view != null) {
            // HACK: can't always get clear focus to actually work. It's just a request.
            boolean focusable = view.isFocusable();
            boolean focusableInTouchMode = view.isFocusableInTouchMode();
            View parent = (View) view.getParent();
            if (parent != null) {
                parent.requestFocus();
            }
            view.clearFocus();
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.setFocusable(focusable);
            view.setFocusableInTouchMode(focusableInTouchMode);
        }
    }

    public static void showKeyboard(View v) {
        if (v != null) {
            v.requestFocus();
            ((InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Turns the string into a drawable of the chosen size
     *
     * @param paint Make sure to set the font type if you wish to use the symbol font.
     * @param width Width of the drawable to create.
     * @param height Height of the drawable to create.
     */
    public static Drawable convertStringToDrawable(Context context, String msg, Paint paint, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect bounds = new Rect();
        paint.getTextBounds(msg, 0, msg.length(), bounds);

        float scaleFactor = (float) width/ bounds.width();
        float currentSize = paint.getTextSize();
        float newSize = currentSize * scaleFactor;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(newSize);
        paint.getTextBounds(msg, 0, msg.length(), bounds);

        float x = (bitmap.getWidth() - bounds.width())/2;
        float y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(msg, x, y, paint);

        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
