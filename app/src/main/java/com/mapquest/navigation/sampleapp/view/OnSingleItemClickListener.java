package com.mapquest.navigation.sampleapp.view;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class OnSingleItemClickListener implements OnItemClickListener {
    private static final long CLICK_THROTTLE_TIME_MILLIS = 1000;
    private long mLastClickTime;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(SystemClock.elapsedRealtime() - mLastClickTime < CLICK_THROTTLE_TIME_MILLIS) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        onSingleItemClick(parent, view, position, id);
    }

    abstract public void onSingleItemClick(AdapterView<?> parent, View view, int position, long id);
}
