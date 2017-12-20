package com.mapquest.navigation.sampleapp.searchahead.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapquest.android.commoncore.log.L;

public abstract class AbstractFragment<T extends FragmentCallbacks> extends Fragment {

    private T mMockCallbacks; //for testing only!

    @SuppressWarnings("unchecked")
    protected T getCallbacks() {
        if (getTargetFragment() instanceof FragmentCallbacks) {
            return (T) getTargetFragment();
        } else if (getActivity() instanceof FragmentCallbacks) {
            return (T) getActivity();
        } else if(mMockCallbacks != null) {
            return (T) mMockCallbacks;
        } else {
            return null;
        }
    }

    /**
     * Used to allow us to mock the getCallbacks() method for unit testing classes that extend AbstractFragment.
     */
    public void setMockCallbacks(T callbacks){
        mMockCallbacks = callbacks;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        L.breadcrumb();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        L.breadcrumb();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Activity activity) {
        L.breadcrumb();
        super.onAttach(activity);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        L.breadcrumb();
        super.onConfigurationChanged(newConfig);
    }

    protected void onCreateRetainInstance() {
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        L.breadcrumb();
        super.onCreate(savedInstanceState);
        onCreateRetainInstance();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        L.breadcrumb();
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        L.breadcrumb();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.breadcrumb();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        L.breadcrumb();
        super.onDestroy();
    }

    @Override
    public void onDestroyOptionsMenu() {
        L.breadcrumb();
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onDestroyView() {
        L.breadcrumb();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        L.breadcrumb();
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        L.breadcrumb();
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        L.breadcrumb();
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onLowMemory() {
        L.breadcrumb();
        super.onLowMemory();
    }

    @Override
    public void onPause() {
        L.breadcrumb();
        super.onPause();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        L.breadcrumb();
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        L.breadcrumb();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        L.breadcrumb();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        L.breadcrumb();
        super.onStart();
    }

    @Override
    public void onStop() {
        L.breadcrumb();
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        L.breadcrumb();
        super.onViewCreated(view, savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        L.breadcrumb();
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void startActivity(Intent intent) {
        L.breadcrumb();
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        L.breadcrumb();
        super.startActivityForResult(intent, requestCode);
    }

    public <V extends View> V findViewById(int id) {
        return findViewById(getView(), id);
    }

    @SuppressWarnings("unchecked")
    public <V extends View> V findViewById(View v, int id) {
        return (V) v.findViewById(id);
    }
}
