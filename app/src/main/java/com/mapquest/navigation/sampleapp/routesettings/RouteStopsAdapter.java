package com.mapquest.navigation.sampleapp.routesettings;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mapquest.navigation.internal.util.ArgumentValidator;
import com.mapquest.navigation.sampleapp.routesettings.views.RouteStopView;

import java.util.ArrayList;
import java.util.List;

public class RouteStopsAdapter extends RecyclerView.Adapter<RouteStopsAdapter.RouteStopViewHolder> {

    public static class RouteStopViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final RouteStopView mRouteStopView;

        public RouteStopViewHolder(RouteStopView routeStopView) {
            super(routeStopView);
            mRouteStopView = routeStopView;
        }
    }

    @NonNull
    private final RouteStopsChangedListener mRouteStopsChangedListener;
    @NonNull
    private List<RouteStop> mRouteStops = new ArrayList<>();

    public RouteStopsAdapter(@NonNull RouteStopsChangedListener routeStopsChangedListener) {
        ArgumentValidator.assertNotNull(routeStopsChangedListener);
        mRouteStopsChangedListener = routeStopsChangedListener;
    }

    public void addRouteStops(List<RouteStop> routeStops) {
        mRouteStops.addAll(routeStops);
        notifyDataSetChanged();
    }

    public void addRouteStop(RouteStop routeStop) {
        mRouteStops.add(routeStop);
        notifyItemInserted(mRouteStops.size() - 1);
        mRouteStopsChangedListener.routeStopAdded(routeStop, mRouteStops);
    }

    @Override
    public RouteStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RouteStopView routeStopView = new RouteStopView(parent.getContext());
        routeStopView.setLayoutParams(new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RouteStopViewHolder(routeStopView);
    }

    @Override
    public void onBindViewHolder(RouteStopViewHolder holder, int position) {
        RouteStop routeStop = mRouteStops.get(position);
        holder.mRouteStopView.setRouteStop(routeStop);
        holder.mRouteStopView.showDeleteButton(position != 0);
        holder.mRouteStopView.setOnRouteStopDeleteClickListener(
                new RouteStopDeleteClickListener(routeStop, position));
    }

    @Override
    public int getItemCount() {
        return mRouteStops.size();
    }

    private class RouteStopDeleteClickListener implements View.OnClickListener {
        @NonNull
        private final RouteStop mRouteStop;
        private final int mPosition;

        private RouteStopDeleteClickListener(@NonNull RouteStop routeStop, int position) {
            mRouteStop = routeStop;
            mPosition = position;
        }

        @Override
        public void onClick(View view) {
            mRouteStops.remove(mRouteStop);
            notifyItemRemoved(mPosition);
            mRouteStopsChangedListener.routeStopRemoved(mRouteStop, mRouteStops);
        }
    }
}
