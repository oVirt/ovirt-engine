package org.ovirt.engine.ui.common.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class WebUtils {

    /**
     * Opens a link on another browser window.
     *
     * @param name
     *            The name of the new window
     * @param url
     *            The URL to open
     */
    public static void openUrlInNewWindow(String name, String url) {
        Window.open(url, name.replace(" ", "_"), null); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static void openRelativeUrlInNewWindow(String name, String relativeUrl) {
        openUrlInNewWindow(name, GWT.getModuleBaseURL() + relativeUrl);
    }

}
