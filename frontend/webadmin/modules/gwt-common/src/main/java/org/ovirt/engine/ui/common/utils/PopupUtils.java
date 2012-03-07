package org.ovirt.engine.ui.common.utils;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;


public class PopupUtils {

    public static void showPopup(final PopupPanel popup, int left, int top) {

        // We need this in order to get the correct offsetWidth and offsetHeight.
        // Until we show the element it is not connected to the DOM and its width and height are 0.
        Style style = popup.getElement().getStyle();
        style.setProperty("height", "auto");
        style.setProperty("width", "auto");
        popup.show();

        // Check that the popup will fit the screen
        int availableHeight = Window.getClientHeight()
                - top;
        int missingHeight = popup.getOffsetHeight() - availableHeight;
        if (missingHeight > 0) {
            // First move the top of the popup to get more space
            // Don't move above top of screen, don't move more than needed
            int moveUpBy = Math.min(top, missingHeight);

            // Update state
            top -= moveUpBy;
            missingHeight -= moveUpBy;
            availableHeight += moveUpBy;

            style.setWidth(popup.getOffsetWidth(), Unit.PX);
            style.setHeight(availableHeight, Unit.PX);

            if (missingHeight > 0) {
                style.setOverflowY(Overflow.SCROLL);
                style.setWidth(popup.getOffsetWidth() + getNativeScrollbarWidth(), Unit.PX);
            }
        }

        if (left + popup.getOffsetWidth() >= Window.getClientWidth()) {
            left = Window.getClientWidth()
                    - popup.getOffsetWidth();
        }

        popup.setPopupPosition(left, top);
        popup.show();

    }

    private static int detectedScrollbarSize = -1;

    private static int getNativeScrollbarWidth() {
        if (detectedScrollbarSize < 0) {
            Element scroller = DOM.createDiv();
            scroller.getStyle().setProperty("width", "50px");
            scroller.getStyle().setProperty("height", "50px");
            scroller.getStyle().setProperty("overflow", "scroll");
            scroller.getStyle().setProperty("position", "absolute");
            scroller.getStyle().setProperty("marginLeft", "-5000px");
            RootPanel.getBodyElement().appendChild(scroller);
            detectedScrollbarSize = scroller.getOffsetWidth()
                    - scroller.getPropertyInt("clientWidth");

            // Use a default if detection fails.
            if (detectedScrollbarSize <= 0) {
                detectedScrollbarSize = 20;
            }

            RootPanel.getBodyElement().removeChild(scroller);

        }
        return detectedScrollbarSize;
    }

}
