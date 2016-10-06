package org.ovirt.engine.ui.common.utils;

import com.google.gwt.dom.client.Element;

public class ElementUtils {

    /**
     * Uses scrollWidth to detect horizontal overflow.
     */
    public static boolean detectHorizontalOverflow(Element element) {
        int scrollWidth = element.getScrollWidth();
        int clientWidth = element.getClientWidth();
        return scrollWidth > clientWidth;
    }

    /**
     * Uses scrollHeight to detect vertical overflow.
     */
    public static boolean detectVerticalOverflow(Element element) {
        int scrollHeight = element.getScrollHeight();
        int clientHeight = element.getClientHeight();
        return scrollHeight > clientHeight;
    }

}
