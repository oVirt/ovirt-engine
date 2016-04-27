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
