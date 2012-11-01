package org.ovirt.engine.ui.frontend.utils;

import com.google.gwt.core.client.GWT;

public class FrontendUrlUtils {

    /**
     * Calculates the root URL of the server from where the application was served, based on
     * {@linkplain GWT#getModuleBaseURL GWT module base URL}.
     * <p>
     * For example, if the application was served from <code>http://www.example.com/foo/bar</code>, it will return
     * <code>http://www.example.com/</code>.
     *
     * @return The root URL of the server, including the trailing slash.
     */
    public static String getRootURL() {
        String moduleURL = GWT.getModuleBaseURL();
        String separator = "://"; //$NON-NLS-1$
        int index = moduleURL.indexOf(separator);
        index = moduleURL.indexOf("/", index + separator.length()); //$NON-NLS-1$
        String result = (index != -1) ? moduleURL.substring(0, index + 1) : moduleURL;
        if (!result.endsWith("/")) { //$NON-NLS-1$
            result += "/"; //$NON-NLS-1$
        }
        return result;
    }

}
