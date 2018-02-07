package com.mapquest.navigation.sampleapp.searchahead.util;

import com.mapquest.android.commoncore.util.ParamUtil;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utilities related to dealing with the "query" field associated with many addresses. query should
 * be either
 * <p>
 * 1) "id:" followed by mqid (for a business)
 * <p>
 * 2) "sic:" followed by mqcode(s) and possibly geo information. examples:
 * <p>
 * category search: "sic:581222,58120880"
 * <p>
 * franchise: "sic:581222F77"
 * <p>
 * geo-category search "sic:581222 ll:39.738453,-104.984853 gcq:A5"
 * <p>
 * 3) blank (for addresses that are not businesses) due to bad data from search ahead, we may get
 * query in a response that is text, like a display string, when it really should be blank
 */
public class AddressQueryUtil {
    public static final String MQ_ID_PREFIX = "id:";
    public static final String MQ_SIC_PREFIX = "sic:";

    public static boolean isIdQuery(String query) {
        return query != null && query.startsWith(MQ_ID_PREFIX)
                && NumberUtils.isNumber(getPortionAfterId(query));
    }

    public static boolean isSicQuery(String query) {
        // TODO: better validation here on the different things a sic query could be?
        return  query != null && query.startsWith(MQ_SIC_PREFIX)
                && !getPortionAfterSic(query).isEmpty();
    }

    public static boolean isValidQuery(String query) {
        return isValidNonTrivialQuery(query) || (query != null && query.isEmpty());
    }

    public static boolean isValidNonTrivialQuery(String query) {
        return isIdQuery(query) || isSicQuery(query);
    }

    public static String extractNumberPortionFromIdQuery(String query) {
        ParamUtil.validateParamTrue(query + "is not an id query", isIdQuery(query));
        return getPortionAfterId(query);
    }

    public static String makeIdQuery(String mqId) {
        ParamUtil.validateParamTrue(mqId + "is not a mqId", isPotentialMqId(mqId));
        return MQ_ID_PREFIX + mqId;
    }

    public static boolean isPotentialMqId(String value) {
        return NumberUtils.isNumber(value);
    }

    private static String getPortionAfterId(String query) {
        return query.replace(MQ_ID_PREFIX, "");
    }

    private static String getPortionAfterSic(String query) {
        return query.replace(MQ_SIC_PREFIX, "");
    }
}
