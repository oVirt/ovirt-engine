package org.ovirt.engine.ui.common.utils;


/**
 * A collection of native functions for executing jquery code.
 *
 * Please have a *really* good reason to use jquery.
 *
 */
public class JqueryUtils {

    /**
     * Fire a mouseenter event at an element found at x,y. Must use jquery because
     * GWT doesn't support 'mouseenter.'
     *
     * @param clientX
     * @param clientY
     * @return
     */
    public static native void fireMouseEnter(int clientX, int clientY)
    /*-{
        var el = $wnd.document.elementFromPoint(clientX, clientY);
        if(el != null && el.nodeType == 3) {
            el = el.parentNode;
            console.log(el.parentNode);
        }
        $wnd.jQuery(el).trigger("mouseenter");
    }-*/;

    /**
     * Is there any open tooltip visible?
     *
     * @return
     */
    public static native boolean anyTooltipVisible() /*-{
        var $tooltip = $wnd.jQuery("div.tooltip:visible");
        if ($tooltip.length == 0) return false;
        return true;
    }-*/;

    /**
     * Get mouse position
     */
    public static native String getMousePosition() /*-{
        return "" + $wnd.mouseX + "," + $wnd.mouseY;
    }-*/;

}
