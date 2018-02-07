package com.mapquest.navigation.sampleapp.searchahead.util;

public class StringUtils {

    /**
     * Append the given string to the buffer if it is not blank, prepended by seperator string if
     * the buffer is not empty.
     *
     * @param buffer Buffer to append to
     * @param separator Separator string
     * @param str String to append
     */
    public static void appendIfNotBlank(final StringBuilder buffer, final String separator,
            final String str) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(str)) {
            if (buffer.length() > 0) {
                buffer.append(separator);
            }
            buffer.append(str);
        }
    }
}
