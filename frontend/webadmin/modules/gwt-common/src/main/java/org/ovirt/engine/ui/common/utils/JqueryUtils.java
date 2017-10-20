package org.ovirt.engine.ui.common.utils;

import com.google.gwt.dom.client.Element;

/**
 * A collection of native functions for executing jQuery code.
 * <p>
 * <em>Please have a *really* good reason to use jQuery!</em>
 */
public class JqueryUtils {

    /**
     * Extract text from HTML. Returned string is never null.
     */
    public static native String getTextFromHtml(String html) /*-{
        return $wnd.jQuery($wnd.jQuery.parseHTML(html)).text();
    }-*/;

    /**
     * Return index of the given element, relative to its sibling elements.
     */
    public static native int index(Element elm) /*-{
        return $wnd.jQuery(elm).index();
    }-*/;

}
