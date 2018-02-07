package com.mapquest.navigation.sampleapp.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.mapquest.navigation.location.LocationProviderAdapter;
import com.mapquest.navigation.sampleapp.Constants;
import com.mapquest.navigation.sampleapp.R;
import com.mapquest.navigation.model.location.Location;

public final class LocationUtil {

    private static Handler mLocationAcquisitionTimeoutHandler = new Handler();

    private static void doAcquireLocationWithTimeout(LocationProviderAdapter locationProviderAdapter, final LocationProviderAdapter.LocationAcquisitionListener listener) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.onLocationAcquired(null); // couldn't acquire a location... allow listener (below) to close dialog and ask if should re-try
            }
        };
        mLocationAcquisitionTimeoutHandler.removeCallbacksAndMessages(null); // first clear any outstanding time-out timers
        mLocationAcquisitionTimeoutHandler.postDelayed(r, Constants.LOCATION_ACQUISITION_TIMEOUT); // start (another) timeout timer...
        locationProviderAdapter.acquireLocation(listener, Constants.VALID_STANDARD_LOCATION_TIME_THRESHOLD, Constants.VALID_STANDARD_LOCATION_ACCURACY_THRESHOLD_METERS);
    }

    public static void acquireLocation(final Context context,
                                        final LocationProviderAdapter locationProviderAdapter,
                                        final LocationProviderAdapter.LocationAcquisitionListener listener) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Locating");
        progressDialog.setMessage("Finding Your Location...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final LocationProviderAdapter.LocationAcquisitionListener locationAcquisitionListener = new LocationProviderAdapter.LocationAcquisitionListener() {
            @Override
            public void onLocationAcquired(Location location) {
                progressDialog.dismiss();
                mLocationAcquisitionTimeoutHandler.removeCallbacksAndMessages(null);

                final LocationProviderAdapter.LocationAcquisitionListener locationAcquisitionListener = this;
                if(location == null) {
                    // null means the call to acquireLocation() timed-out...
                    //
                    final AlertDialog retryDialog = new AlertDialog.Builder(context)
                            .setTitle("Cannot Locate")
                            .setMessage("Please be sure you have enabled Location in your Settings.\n\n" +
                                    "Try again now to find your location?")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // "OK" was clicked (i.e. should "Retry...")
                                    progressDialog.show();
                                    doAcquireLocationWithTimeout(locationProviderAdapter, locationAcquisitionListener);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // "Cancel" was clicked (i.e. don't bother re-trying...)
                                    // (nothing left to do here)
                                }
                            })
                            .show();
                } else {
                    listener.onLocationAcquired(location);
                }
            }
        };
        doAcquireLocationWithTimeout(locationProviderAdapter, locationAcquisitionListener);
    }
}
