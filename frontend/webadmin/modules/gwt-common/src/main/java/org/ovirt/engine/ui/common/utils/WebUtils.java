package org.ovirt.engine.ui.common.utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class WebUtils {
    /**
     * constant for the 'scrollbars' window option.
     */
    public static final String OPTION_SCROLLBARS = "scrollbars"; //$NON-NLS-1$

    /**
     * The default options to pass to Window.open when none are specified.
     */
    public static final String DEFAULT_OPTIONS = OPTION_SCROLLBARS;

    /**
     * Opens a link on another browser window.
     *
     * @param name
     *            The name of the new window
     * @param url
     *            The URL to open
     */
    public static void openUrlInNewWindow(final String name, final String url) {
        openUrlInNewWindow(name, url, DEFAULT_OPTIONS);
    }

    /**
     * Opens a link on another browser window.
     *
     * @param name The name of the new window
     * @param url The URL to open
     * @param options The options to pass to the window, option are described here:<br/>
     * {@link https://developer.mozilla.org/en-US/docs/DOM/window.open}
     */
    public static void openUrlInNewWindow(final String name, final String url, final String options) {
        Window.open(url, name.replace(" ", "_"), options); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static void openRelativeUrlInNewWindow(String name, String relativeUrl) {
        openUrlInNewWindow(name, GWT.getModuleBaseURL() + relativeUrl);
    }

}
