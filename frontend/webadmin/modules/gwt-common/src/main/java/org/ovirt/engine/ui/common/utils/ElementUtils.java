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

    /**
     * Check up this Element's ancestor tree, and return true if we find a null parent.
     * @return true if a null parent is found, false if this Element has body as an ancestor
     * (meaning it's still attached).
     */
    public static boolean hasNullAncestor(Element element) {
        Element parent = element.getParentElement();
        while (parent != null) {
            if (parent.getTagName().equalsIgnoreCase("body")) { //$NON-NLS-1$
                return false;
            }
            parent = parent.getParentElement();
        }
        return true;
    }

    /**
     * Is an Element an ancestor of another Element?
     */
    public static boolean hasAncestor(Element element, Element ancestor) {

        if (ancestor == null || element == null) {
            return false;
        }

        Element parent = element.getParentElement();
        while (parent != null) {
            if (parent.getInnerHTML().equals(ancestor.getInnerHTML())) {
                return true;
            }
            parent = parent.getParentElement();
        }
        return false;
    }

    /**
     * Return any element found at point x, y.
     */
    public static native Element getElementFromPoint(int clientX, int clientY)
    /*-{
        var el = $wnd.document.elementFromPoint(clientX, clientY);
        if(el != null && el.nodeType == 3) {
            el = el.parentNode;
        }
        return el;
    }-*/;

}
