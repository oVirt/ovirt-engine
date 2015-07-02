package org.ovirt.engine.ui.common.utils;

/**
 * A collection of native functions for executing jQuery code.
 * <p>
 * <em>Please have a *really* good reason to use jQuery!</em>
 */
public class JqueryUtils {

    /**
     * Fire a mouseenter event at an element found at [x,y].
     * Must use jQuery because GWT doesn't support mouseenter.
     */
    public static native void fireMouseEnter(int clientX, int clientY) /*-{
        var el = $wnd.document.elementFromPoint(clientX, clientY);
        if(el != null && el.nodeType == 3) {
            el = el.parentNode;
            console.log(el.parentNode);
        }
        $wnd.jQuery(el).trigger("mouseenter");
    }-*/;

    /**
     * Is there any open tooltip visible?
     */
    public static native boolean anyTooltipVisible() /*-{
        var $tooltip = $wnd.jQuery("div.tooltip:visible");
        return $tooltip.length != 0;
    }-*/;

    /**
     * Get mouse position.
     */
    public static native String getMousePosition() /*-{
        return "" + $wnd.mouseX + "," + $wnd.mouseY;
    }-*/;

    /**
     * Extract text from HTML. Returned string is never null.
     */
    public static native String getTextFromHtml(String html) /*-{
        return $wnd.jQuery(html).text();
    }-*/;

}
