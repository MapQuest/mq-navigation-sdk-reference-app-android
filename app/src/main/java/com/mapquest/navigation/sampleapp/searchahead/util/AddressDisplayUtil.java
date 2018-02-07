package com.mapquest.navigation.sampleapp.searchahead.util;

import android.content.res.Resources;

import com.mapquest.android.commoncore.util.ParamUtil;
import com.mapquest.navigation.sampleapp.searchahead.Address;
import com.mapquest.navigation.sampleapp.searchahead.SearchAheadResult;

import org.apache.commons.lang3.StringUtils;

/**
 * Separates logic for building strings to represent addresses
 */
public class AddressDisplayUtil {

    private static AddressDisplayUtil sInstance;

    public static AddressDisplayUtil forResources(Resources resources) {
        ParamUtil.validateParamNotNull(resources);
        if (sInstance == null) {
            sInstance = new AddressDisplayUtil(resources);
        }
        return sInstance;
    }

    protected AddressDisplayUtil(Resources resources) {
        ParamUtil.validateParamsNotNull(resources);
    }

    public String getPrimaryString(Address address) {
        ParamUtil.validateParamNotNull(address);
        if (nameContainsStreetAddress(address)) {
            return address.getStreet();
        }
        if (address.hasName()) {
            return address.getData().getName();
        }
        if (StringUtils.isNotBlank(address.getStreet())) {
            return address.getStreet();
        }
        if (StringUtils.isNotBlank(address.getLocality())) {
            return getLocalityString(address);
        }
        if (StringUtils.isNotBlank(address.getRegionCode())) {
            return address.getRegionCode();
        }
        if (StringUtils.isNotBlank(address.getPostalCode())) {
            return address.getPostalCode();
        }
        if (StringUtils.isNotBlank(address.getCountryCode())) {
            return address.getCountryCode();
        }
        return "";
    }

    /**
     * Utility method for getting locality.
     */
    public String getLocalityString(Address address) {
        ParamUtil.validateParamNotNull(address);
        ParamUtil.validateParamTrue("Must have locality", address.getLocality() != null);
        StringBuilder buffer = new StringBuilder(address.getLocality());
        com.mapquest.navigation.sampleapp.searchahead.util.StringUtils.appendIfNotBlank(buffer, ", ", address.getRegionCode());
        com.mapquest.navigation.sampleapp.searchahead.util.StringUtils.appendIfNotBlank(buffer, " ", address.getPostalCode());
        return buffer.toString();
    }

    /**
     * Returns the displayString, except for POI type search results.
     * <p>
     * If the taxonomy type is POI (because displayString is returned instead because the
     * displayString JSON for POI is "POI name + address" which we can derive from the other pieces
     * of data).
     */
    public String getDisplayString(SearchAheadResult searchAheadResult) {
        if (searchAheadResult.getTaxonomyType() != SearchAheadResult.TaxonomyType.POI &&
                !StringUtils.isBlank(searchAheadResult.getDisplayString())) {
            return searchAheadResult.getDisplayString();
        }

        return getAddressPrimaryString(searchAheadResult);
    }

    /**
     * Defaults to address.primaryString, falls back to displayString
     */
    public String getAddressPrimaryString(SearchAheadResult searchAheadResult) {
        if (searchAheadResult.getAddress() != null
                && !StringUtils.isBlank(getPrimaryString(searchAheadResult.getAddress()))) {
            return getPrimaryString(searchAheadResult.getAddress());
        } else {
            return org.apache.commons.lang3.StringUtils
                    .defaultIfEmpty(searchAheadResult.getDisplayString(), "");
        }
    }

    private static boolean nameContainsStreetAddress(Address address) {
        return StringUtils.isNotBlank(address.getStreet()) && address.hasName()
                && address.getData().getName().contains(address.getStreet());
    }
}
