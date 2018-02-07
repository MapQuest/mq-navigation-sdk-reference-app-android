package com.mapquest.navigation.sampleapp.searchahead;

import android.support.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.mapquest.android.commoncore.dataclient.BaseNetworkClient;
import com.mapquest.android.commoncore.util.ParamUtil;
import com.mapquest.android.commoncore.util.VolleyUtil;
import com.mapquest.navigation.sampleapp.ISampleAppConfiguration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SearchActivityServiceClient extends BaseNetworkClient<Void, Void> {

    private static final String HEADER_PLATFORM_ARG = "x-mq-platform-id";
    private static final String HEADER_PLATFORM_TYPE = "android";
    private static final String HEADER_DEVICE_ID_ARG = "x-mq-user-id";

    @NonNull
    private final ISampleAppConfiguration mAceConfiguration;

    public SearchActivityServiceClient(@NonNull ISampleAppConfiguration aceConfiguration) {
        ParamUtil.validateParamNotNull(aceConfiguration);
        mAceConfiguration = aceConfiguration;
    }

    private Request<?> newRequest(URL url, Response.Listener<Void> listener, Response.ErrorListener errorListener) {
        return new FeedbackRequest(url.toString(), mAceConfiguration, listener, errorListener);
    }

    @Override
    public Request<?> newRequest(URL url, Void requestData,
                                 Response.Listener<Void> listener,
                                 Response.ErrorListener errorListener) {
        return newRequest(url, listener, errorListener);
    }

    private static class FeedbackRequest extends Request<Void> {

        private Response.Listener<Void> mListener;
        private final ISampleAppConfiguration mAceConfiguration;

        public FeedbackRequest(String url, @NonNull ISampleAppConfiguration aceConfiguration,
                               Response.Listener<Void> listener, Response.ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            ParamUtil.validateParamNotNull(aceConfiguration);
            mListener = listener;
            mAceConfiguration = aceConfiguration;
        }

        @Override
        protected Response<Void> parseNetworkResponse(NetworkResponse response) {
            return VolleyUtil.responseSuccess(null, response);
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put(HEADER_PLATFORM_ARG, HEADER_PLATFORM_TYPE);
            headers.put(HEADER_DEVICE_ID_ARG, mAceConfiguration.getPersistentInstallId());
            return headers;
        }

        @Override
        protected void deliverResponse(Void response) {
            if (mListener != null) {
                mListener.onResponse(response);
            }
        }
    }
}
