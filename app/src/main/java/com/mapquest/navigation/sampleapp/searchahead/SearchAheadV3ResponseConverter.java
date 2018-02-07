package com.mapquest.navigation.sampleapp.searchahead;

import com.mapquest.android.commoncore.util.ParamUtil;
import com.mapquest.android.searchahead.model.response.AddressProperties;
import com.mapquest.android.searchahead.model.response.Feedback;
import com.mapquest.android.searchahead.model.response.Place;
import com.mapquest.android.searchahead.model.response.RecordType;

import java.util.List;

class SearchAheadV3ResponseConverter {

    private static final String MQ_SEARCH_AHEAD_EXPRESSION = "X-MQ-SEARCH-EXP";
    private static SearchAheadV3ResponseConverter mInstance;

    private SearchAheadV3ResponseConverter() {}

    public static SearchAheadV3ResponseConverter getInstance() {
        if (mInstance == null) {
            mInstance = new SearchAheadV3ResponseConverter();
        }
        return mInstance;
    }

    /**
     * Converts new v3 search ahead models to our existing models
     */
    public SearchAheadResponse toSearchAheadResponse(
            com.mapquest.android.searchahead.model.response.SearchAheadResponse searchAheadV3Response) {
        ParamUtil.validateParamsNotNull(searchAheadV3Response);

        SearchAheadResponse searchAheadResponse = new SearchAheadResponse();

        if (searchAheadV3Response.getExpression() != null) {
            searchAheadResponse.getHeaders().put(MQ_SEARCH_AHEAD_EXPRESSION,
                    searchAheadV3Response.getExpression());
        }

        for (com.mapquest.android.searchahead.model.response.SearchAheadResult searchAheadResult : searchAheadV3Response
                .getResults()) {
            SearchAheadResult newSearchAheadResult = buildSearchResult(searchAheadResult);
            searchAheadResponse.addResult(newSearchAheadResult);
        }

        if (searchAheadV3Response.getFeedback() != null && searchAheadResponse.getResults() != null
                && !searchAheadResponse.getResults().isEmpty()) {
            searchAheadResponse.setFeedback(
                    buildSearchAheadFeedback(searchAheadV3Response.getFeedback(), searchAheadResponse.getResults()));
        }

        return searchAheadResponse;
    }

    private SearchAheadFeedback buildSearchAheadFeedback(Feedback feedback, List<SearchAheadResult> results) {
        SearchAheadFeedback newFeedback = new SearchAheadFeedback(feedback.getResultClickedUrlTemplate(),
                feedback.getResultViewedUrlTemplate(), results);

        return newFeedback;
    }

    private SearchAheadResult buildSearchResult(
            com.mapquest.android.searchahead.model.response.SearchAheadResult searchAheadResult) {

        SearchAheadResult newSearchAheadResult = new SearchAheadResult();

        newSearchAheadResult.setAddress(buildAddress(searchAheadResult));
        if (searchAheadResult.getRecordType().equals(RecordType.AIRPORT) ||
                searchAheadResult.getRecordType().equals(RecordType.POI)) {
            newSearchAheadResult.setDisplayString(searchAheadResult.getName());
        } else {
            newSearchAheadResult.setDisplayString(searchAheadResult.getDisplayString());
        }
        newSearchAheadResult.addTaxonomyType(searchAheadResult.getRecordType());

        newSearchAheadResult.addId(searchAheadResult.getSearchAheadId().getFullId());

        // Sets a sic code if it has one if not it sets an mqId if that exists
        if (searchAheadResult.getSicCodes() != null && !searchAheadResult.getSicCodes().isEmpty()) {
            newSearchAheadResult.setQuery("sic:" + searchAheadResult.getSicCodes().get(0));
        } else if (searchAheadResult.getSearchAheadId().isMqId()) {
            newSearchAheadResult.setQuery("id:" + searchAheadResult.getSearchAheadId().getMqId());
        }

        // No id provided with new search ahead result model might end up being the slug or some other unique id
        // newSearchAheadResult.addId(null);
        // No dictionary items in search ahead v3 result
        newSearchAheadResult.setDictionary(null);

        return newSearchAheadResult;
    }

    private Address buildAddress(
            com.mapquest.android.searchahead.model.response.SearchAheadResult searchAheadResult) {

        Address address = new Address();
        Place place = searchAheadResult.getPlace();
        if (place != null) {
            AddressProperties addressProperties = place.getAddressProperties();

            // The display lat lng is the only value returned as of search ahead v3
            address.setDisplayGeoPoint(place.getLatLng());
            address.setCounty(addressProperties.getCounty());
            address.setCountryFullName(addressProperties.getCountry());
            address.setCountryCode(addressProperties.getCountryCode());
            address.setStreet(addressProperties.getStreet());
            address.setPostalCode(addressProperties.getPostalCode());

            if (searchAheadResult.getSearchAheadId().isMqId()) {
                address.getData().setId(searchAheadResult.getSearchAheadId().getMqId());
            }

            address.getData().setSlug(searchAheadResult.getSlug());
            if (searchAheadResult.getName() != null) {
                address.getData().setName(searchAheadResult.getName());
            }
            address.setRegionFullName(addressProperties.getState());
            address.setRegionCode(addressProperties.getStateCode());
            address.setLocality(addressProperties.getCity());
            address.setNeighborhood(addressProperties.getNeighborhood());

            address.setRecordType(searchAheadResult.getRecordType());
        }

        return address;
    }
}
