package org.ovirt.engine.ui.webadmin.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class WebUtils {
    /**
     * Open a link on another window
     *
     * @param name
     *            The name of the new window
     * @param url
     *            The URL to open
     */
    public static void openUrlInNewWindow(String name, String url) {
        Window.open(url, name.replace(" ", "_"), null);
    }

    public static void openRelativeUrlInNewWindow(String name, String relativeUrl) {
        openUrlInNewWindow(name, GWT.getModuleBaseURL() + relativeUrl);
    }

}
