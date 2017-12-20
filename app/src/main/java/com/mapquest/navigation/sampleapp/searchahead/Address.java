package com.mapquest.navigation.sampleapp.searchahead;

import com.mapquest.android.commoncore.model.LatLng;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Address implements Cloneable {

    // this is either a display lat lng - suitable for displaying an address/poi/airport/place on the map
    // OR an actual lat lng - for when this Address is used to represent something like current location
    private LatLng mDisplayOrActualGeoPoint;

    private String mStreet;
    private String mLocality;
    private String mRegionFullName;
    private String mRegionCode;
    private String mCounty;
    private String mPostalCode;
    private String mCountryFullName;
    private String mCountryCode;
    private String mNeighborhood;

    // This value comes from the SAv3 response. It will only be populated on
    // addresses returned as part of a search ahead response.
    // This class is not a good home for this value and it will need to be
    // moved to a better location at a later time.
    private String mRecordType;

    public AddressData mData;

    public Address() {
        mData = new AddressData();
    }

    public boolean hasName() {
        return StringUtils.isNotBlank(mData.getName());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Address)) {
            return false;
        }

        Address addr = (Address) o;
        if (mDisplayOrActualGeoPoint != null
                && !(mDisplayOrActualGeoPoint.equals(addr.getDisplayGeoPoint()))
                || mNeighborhood != null && !mNeighborhood.equals(addr.mNeighborhood)
                || mCountryFullName != null && !mCountryFullName.equals(addr.mCountryFullName)
                || mCounty != null && !mCounty.equals(addr.mCounty)
                || mCountryCode != null && !mCountryCode.equals(addr.mCountryCode)
                || mLocality != null && !mLocality.equals(addr.mLocality)
                || mPostalCode != null && !mPostalCode.equals(addr.mPostalCode)
                || mRecordType != null && !mRecordType.equals(addr.mRecordType)
                || mRegionFullName != null && !mRegionFullName.equals(addr.mRegionFullName)
                || mRegionCode != null && !mRegionCode.equals(addr.mRegionCode)
                || mStreet != null && !mStreet.equals(addr.mStreet)) {
            return false;
        }

        return StringUtils.equals(mData.getName(), addr.mData.getName());
    }

    public boolean isInternational() {
        return mCountryCode != null && !"US".equals(mCountryCode);
    }

    public void setId(final String mqid) {
        if (!StringUtils.isBlank(mqid)) {
            mData.setId(mqid);
        }
    }

    public LatLng getDisplayGeoPoint() {
        return mDisplayOrActualGeoPoint;
    }

    public void setDisplayGeoPoint(LatLng displayGeoPoint) {
        mDisplayOrActualGeoPoint = displayGeoPoint;
    }

    public String getStreet() {
        return mStreet;
    }

    public void setStreet(String street) {
        mStreet = street;
    }

    public String getNeighborhood() {
        return mNeighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        mNeighborhood = neighborhood;
    }

    public String getLocality() {
        return mLocality;
    }

    public void setLocality(String locality) {
        mLocality = locality;
    }

    public String getRegionFullName() {
        return mRegionFullName;
    }

    public void setRegionFullName(String region) {
        mRegionFullName = region;
    }

    public String getRegionCode() {
        return mRegionCode;
    }

    public void setRegionCode(String region) {
        mRegionCode = region;
    }

    public String getCounty() {
        return mCounty;
    }

    public void setCounty(String county) {
        mCounty = county;
    }

    public String getPostalCode() {
        return mPostalCode;
    }

   public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }

    public String getCountryFullName() {
        return mCountryFullName;
    }

    public void setCountryFullName(String country) {
        mCountryFullName = country;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public void setCountryCode(String country) {
        mCountryCode = country;
    }

    /**
     * @return nonnull AddressData (may be "empty")
     */
    public AddressData getData() {
        return mData;
    }

    public void setData(AddressData data) {
        mData = data != null ? data : new AddressData();
    }

    /**
     * This method should only be called as part of the address response parsing in the SearchAheadV3ResponseConverter
     * class. Modifying this value anywhere else will lead to incorrect data reporting and potential crashes.
     *
     * @param type type associated with this record
     */
    public void setRecordType(String type) {
        mRecordType = type;
    }
}
