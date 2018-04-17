package com.mapquest.navigation.sampleapp.routesettings.views;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapquest.navigation.model.location.Coordinate;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.sampleapp.routesettings.RouteStop;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouteStopView extends ConstraintLayout {

    private static final DecimalFormat COORDINATE_DECIMAL_FORMAT = new DecimalFormat("##.000000");

    @BindView(R.id.routeStopTextView)
    protected TextView mRouteStopTextView;

    @BindView(R.id.routeStopCoordinateTextView)
    protected TextView mCoordinateTextView;

    @BindView(R.id.routeStopDeleteButton)
    protected ImageButton mRouteStopDeleteButton;

    public RouteStopView(Context context) {
        this(context, null);
    }

    public RouteStopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RouteStopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.route_stop_view, this);
        ButterKnife.bind(this);
    }

    public void showDeleteButton(boolean show) {
        mRouteStopDeleteButton.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void setOnRouteStopDeleteClickListener(OnClickListener listener) {
        mRouteStopDeleteButton.setOnClickListener(listener);
    }

    public void setRouteStop(RouteStop routeStop) {
        mRouteStopTextView.setText(routeStop.getDisplayText());
        mCoordinateTextView.setText(buildCoordinateString(routeStop.getCoordinate()));
    }

    private String buildCoordinateString(Coordinate coordinate) {
        return COORDINATE_DECIMAL_FORMAT.format(coordinate.getLatitude()) + ", " +
                COORDINATE_DECIMAL_FORMAT.format(coordinate.getLongitude());
    }
}
