package org.ovirt.engine.ui.common.utils;

import com.google.gwt.http.client.Response;

public class HttpUtils {

    // IE9 "empty string on missing header" fix isolated to this method
    // If allowBlank == true, method may return empty string; otherwise return null
    public static String getHeader(Response response, String headerKey, boolean allowBlank) {
        String value = response.getHeader(headerKey);
        if (!allowBlank && "".equals(value)) { //$NON-NLS-1$
            value = null;
        }
        return value;
    }

    // shortcut for allowBlank == false
    public static String getHeader(Response response, String headerKey) {
        return getHeader(response, headerKey, false);
    }
}
