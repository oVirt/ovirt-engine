package org.ovirt.engine.ui.common.utils;

import com.google.gwt.user.client.Window;

public class WebUtils {

    /**
     * Opens a link on another browser tab.
     *
     * @param name
     *            The name of the new window
     * @param url
     *            The URL to open
     */
    public static void openUrlInNewTab(final String url) {
        Window.open(url, "_blank", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
