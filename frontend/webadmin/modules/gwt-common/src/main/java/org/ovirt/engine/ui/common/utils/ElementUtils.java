package org.ovirt.engine.ui.common.utils;

import com.google.gwt.dom.client.Element;

public class ElementUtils {

    /**
     * Uses scrollWidth with temporary CSS 'overflow:auto' to detect horizontal overflow.
     */
    public static boolean detectOverflowUsingScrollWidth(Element element) {
        int scrollWidthBefore = element.getScrollWidth();
        String overflowValue = element.getStyle().getProperty("overflow"); //$NON-NLS-1$
        element.getStyle().setProperty("overflow", "auto"); //$NON-NLS-1$ //$NON-NLS-2$

        int scrollWidthAfter = element.getScrollWidth();
        int clientWidthAfter = element.getClientWidth();
        element.getStyle().setProperty("overflow", overflowValue); //$NON-NLS-1$

        return scrollWidthAfter > scrollWidthBefore || scrollWidthAfter > clientWidthAfter;
    }

    /**
     * Uses clientHeight with temporary CSS 'whiteSpace:normal' to detect vertical overflow.
     * <p>
     * This is necessary due to some browsers (Firefox) having issues with scrollWidth (e.g. elements with CSS 'display'
     * other than 'block' have incorrect scrollWidth value).
     */
    public static boolean detectOverflowUsingClientHeight(Element parent) {
        int clientHeightBefore = parent.getClientHeight();
        String whiteSpaceValue = parent.getStyle().getProperty("whiteSpace"); //$NON-NLS-1$
        parent.getStyle().setProperty("whiteSpace", "normal"); //$NON-NLS-1$ //$NON-NLS-2$
        int scrollHeightAfter = parent.getScrollHeight();
        int clientHeightAfter = parent.getClientHeight();
        parent.getStyle().setProperty("whiteSpace", whiteSpaceValue); //$NON-NLS-1$

        return clientHeightAfter > clientHeightBefore || clientHeightAfter < scrollHeightAfter;
    }

}
