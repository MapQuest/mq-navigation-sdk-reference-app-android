package com.mapquest.navigation.sampleapp.searchahead;

import com.android.volley.Request;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SearchActivityServicePerformer {

    private static final String VIEWED_REPLACEMENT_REGEX = "{/id*}";
    private static final String CLICKED_REPLACEMENT_REGEX = "{/id}";
    private static final String URL_ITEM_SEPARATOR = "/";

    private SearchActivityServiceClient mClient;

    public SearchActivityServicePerformer(SearchActivityServiceClient client) {
        mClient = client;
    }

    public void makeClickedSearchActivityRequest(SearchAheadFeedback feedback, String result) {
        URL url = buildClickedUrl(feedback, result);
        if (url != null) {
            Request<?> request = mClient.newRequest(url, null, null, null);
            mClient.issueRequest(request);
        }
    }

    public void makeViewedSearchActivityRequest(SearchAheadFeedback feedback, List<SearchAheadResult> results) {
        URL url = buildViewedUrl(feedback, results);
        if (url != null) {
            Request<?> request = mClient.newRequest(url, null, null, null);
            mClient.issueRequest(request);
        }
    }

    private URL buildViewedUrl(SearchAheadFeedback feedback, List<SearchAheadResult> results) {
        if (results != null && !results.isEmpty() && StringUtils.isNotBlank(feedback.getResultViewedUrlTemplate())) {
            try {
                StringBuilder resultString = new StringBuilder(URL_ITEM_SEPARATOR);
                for (SearchAheadResult result : results) {
                    if (StringUtils.isNotBlank(result.getIds().get(0))) {
                        resultString.append(result.getIds().get(0));
                        resultString.append(URL_ITEM_SEPARATOR);
                    }
                }
                resultString.deleteCharAt(resultString.lastIndexOf(URL_ITEM_SEPARATOR));

                String url = feedback.getResultViewedUrlTemplate().replace(VIEWED_REPLACEMENT_REGEX, resultString);

                return new URL(url);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    private URL buildClickedUrl(SearchAheadFeedback feedback, String result) {
        if (StringUtils.isNotBlank(result) && StringUtils.isNotBlank(feedback.getResultClickedUrlTemplate())) {
            try {
                StringBuilder resultString = new StringBuilder(URL_ITEM_SEPARATOR);
                resultString.append(result);

                String url = feedback.getResultClickedUrlTemplate().replace(CLICKED_REPLACEMENT_REGEX, resultString);

                return new URL(url);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }
}
