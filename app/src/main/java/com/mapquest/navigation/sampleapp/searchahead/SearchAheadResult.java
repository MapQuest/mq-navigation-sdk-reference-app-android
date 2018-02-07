package com.mapquest.navigation.sampleapp.searchahead;

import android.support.annotation.Nullable;

import com.mapquest.android.commoncore.log.L;
import com.mapquest.android.commoncore.util.ParamUtil;
import com.mapquest.navigation.sampleapp.searchahead.util.AddressQueryUtil;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class SearchAheadResult {

    private List<String> mDictionary;
    private String mDisplayString;
    // see AddressQueryUtil for a description of query. we will enforce this is always one of the 3
    // valid query values (therefore, it cannot be null)
    private String mQuery;
    private List<TaxonomyType> mTaxonomyTypes;
    private List<String> mIds;
    @Nullable
    private Address mAddress;

    // added just for logging purposes
    private float mScore = -1;
    private String mMqCode;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public enum TaxonomyType {

        Airport("Airport"),
        Address("Address"),
        Category("Category"),
        Landmark("Landmark"),
        Franchise("Franchise"),
        Place("Place"),
        POI("POI"),
        Neighborhood("Neighborhood"),
        City("City"),
        County("County"),
        State("State"),
        Country("Country"),
        PostalCode("PostalCode");

        private final String mValue;

        TaxonomyType(String value) {
            mValue = value;
        }

        public String value() {
            return mValue;
        }

        public static TaxonomyType create(String value) {
            if (Airport.value().equalsIgnoreCase(value)) {
                return Airport;
            } else if (Address.value().equalsIgnoreCase(value)) {
                return Address;
            } else if (Category.value().equalsIgnoreCase(value)) {
                return Category;
            } else if (Landmark.value().equalsIgnoreCase(value)) {
                return Landmark;
            } else if (Franchise.value().equalsIgnoreCase(value)) {
                return Franchise;
            } else if (Place.value().equalsIgnoreCase(value)) {
                return Place;
            } else if (POI.value().equalsIgnoreCase(value)) {
                return POI;
            } else if (Neighborhood.value().equalsIgnoreCase(value)) {
                return Neighborhood;
            } else if (City.value().equalsIgnoreCase(value)) {
                return City;
            } else if (State.value().equalsIgnoreCase(value)) {
                return State;
            } else if (County.value().equalsIgnoreCase(value)) {
                return County;
            } else if (Country.value().equalsIgnoreCase(value)) {
                return Country;
            } else if (PostalCode.value().equalsIgnoreCase(value)) {
                return PostalCode;
            }

            L.w("Unrecognized taxonomy type: " + value);
            return Place;
        }
    }

    public String getQuery() {
        if (AddressQueryUtil.isValidNonTrivialQuery(mQuery)) {
            return mQuery;
        } else if (mAddress != null) {
            // this will return one of the 3 possible query values (id, sic, or blank)
            return mAddress.getData().getQuery();
        } else {
            return "";
        }
    }

    public void addTaxonomyType(String value) {
        if (mTaxonomyTypes == null) {
            mTaxonomyTypes = new ArrayList<>();
        }
        mTaxonomyTypes.add(TaxonomyType.create(value));
    }

    public TaxonomyType getTaxonomyType() {
        TaxonomyType type = TaxonomyType.Address;
        if (mTaxonomyTypes != null && mTaxonomyTypes.size() > 0) {
            type = mTaxonomyTypes.get(0);
        }
        return type;
    }

    public void addId(String id) {
        if (getIds() == null) {
            setIds(new ArrayList<String>());
        }
        getIds().add(id);
    }

    public String getDisplayString() {
        return mDisplayString;
    }

    public List<String> getDictionary() {
        return mDictionary;
    }

    public void setDictionary(List<String> dictionary) {
        mDictionary = dictionary;
    }

    public void setDisplayString(String displayString) {
        mDisplayString = displayString;
    }

    public void setQuery(String query) {
        ParamUtil.validateParamTrue(query + " is invalid query value",
                AddressQueryUtil.isValidQuery(query));
        mQuery = query;
    }

    public List<TaxonomyType> getTaxonomyTypes() {
        return mTaxonomyTypes;
    }

    public void setTaxonomyTypes(List<TaxonomyType> taxonomyTypes) {
        mTaxonomyTypes = taxonomyTypes;
    }

    public List<String> getIds() {
        return mIds;
    }

    public void setIds(List<String> ids) {
        mIds = ids;
    }

    @Nullable
    public Address getAddress() {
        return mAddress;
    }

    public void setAddress(Address address) {
        mAddress = address;
    }

    public float getScore() {
        return mScore;
    }

    public void setScore(float score) {
        mScore = score;
    }

    public String getMqCode() {
        return mMqCode;
    }

    public void setMqCode(String mqCode) {
        mMqCode = mqCode;
    }

}
