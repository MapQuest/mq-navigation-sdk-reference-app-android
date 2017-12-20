package com.mapquest.navigation.sampleapp.searchahead;

import com.mapquest.navigation.sampleapp.searchahead.util.AddressQueryUtil;

import org.apache.commons.lang3.StringUtils;

public class AddressData {

    private String mName;
    private String mId = "";
    private String mSlug;

    public AddressData() { }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id != null ? id : "";
    }

    public String getSlug() {
        return mSlug;
    }

    public void setSlug(String slug) {
        mSlug = slug;
    }

    public boolean hasSlug() {
        return StringUtils.isNotBlank(mSlug);
    }

    /**
     * Currently it's an MQ ID when it's of the form "id:[number]" or "[number]".
     */
    public boolean hasMqId() {
        return AddressQueryUtil.isIdQuery(mId) || AddressQueryUtil.isPotentialMqId(mId);
    }

    public String getMqId() {
        if (AddressQueryUtil.isIdQuery(mId)) {
            return AddressQueryUtil.extractNumberPortionFromIdQuery(mId);
        } else if (AddressQueryUtil.isPotentialMqId(mId)) {
            return mId;
        } else {
            return "";
        }
    }

    /**
     * @return an id query ("id:<the mq id>") if we have one stored, or can build one. blank
     *         otherwise. use the hasMqId method to determine if this will return a valid query
     *         value.
     */
    public String getIdQuery() {
        if (AddressQueryUtil.isIdQuery(mId)) {
            return mId;
        } else if (AddressQueryUtil.isPotentialMqId(mId)) {
            return AddressQueryUtil.makeIdQuery(mId);
        } else {
            return "";
        }
    }

    public boolean hasSicQuery() {
        return AddressQueryUtil.isSicQuery(mId);
    }

    /**
     * @return a sec query ("sic:<stuff>") if we have one stored. blank otherwise. use the
     *         hasSicQuery method to determine if this will return a valid query value.
     */
    public String getSicQuery() {
        return AddressQueryUtil.isSicQuery(mId) ? mId : "";
    }

    /**
     * no matter what (potentially bad) data is in mId, this always returns a valid (although
     * potentially trivial) query.
     * 
     * @return query if we have one stored. id query if we have a mqid stored. blank if we have
     *         nothing, or bad data.
     */
    public String getQuery() {
        return hasSicQuery() ? getSicQuery() : hasMqId() ? getIdQuery() : "";
    }
}
