package org.ovirt.engine.ui.common.utils;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;

//TODO: add support to RTL locale
public class PopupUtils {

    public static void adjustPopupLocationToFitScreenAndShow(final PopupPanel popup,
            int left,
            int top,
            MenuBar parentMenu, int itemHeight) {

        Style style = popup.getElement().getStyle();

        style.setLeft(0, Unit.PX);
        style.setTop(0, Unit.PX);
        style.setProperty("height", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
        style.setProperty("width", "auto"); //$NON-NLS-1$ //$NON-NLS-2$

        popup.show();
        adjustPopupLocationToFitScreenAndShow(popup, left, top, -parentMenu.getOffsetWidth(), 0, itemHeight);
    }

    private static void adjustPopupLocationToFitScreenAndShow(final PopupPanel popup,
            int relativeLeft,
            int relativeTop,
            int relativeWidth,
            int relativeHeight,
            int itemHeight) {
        Style style = popup.getElement().getStyle();

        /* We need this code because the gwt MenuBar- openPopupMethod, sets the position of the subMenu popup. It can't be overridden.
         * If the menu doesn't fit into the screen, setting the position causes change in its width and height.
         * Here we need the real "auto" width/height, thats why we need to change the left/top so the popup will fit the screen.
         */
        style.setLeft(0, Unit.PX);
        style.setTop(0, Unit.PX);
        style.setProperty("height", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
        style.setProperty("width", "auto"); //$NON-NLS-1$ //$NON-NLS-2$

        popup.show();

        // Calculate top position for the popup

        int top = relativeTop;

        // Make sure scrolling is taken into account
        int windowTop = Window.getScrollTop();
        int windowBottom = Window.getScrollTop() + Window.getClientHeight();

        // Distance from the top edge of the window to the top edge of the
        // relative object
        int distanceFromWindowTop = top - windowTop;

        // Distance from the bottom edge of the window to the bottom edge of
        // the relative object
        int distanceToWindowBottom = windowBottom
                - (top + relativeHeight);

        // If there is not enough space for the popup's height below the
        // relative target and there IS enough space for the popup's height above the
        // relative target, then then position the popup above the relative object. However, if there
        // is not enough space on either side, then stick with displaying the
        // popup below the relative object.
        if (distanceToWindowBottom >= popup.getOffsetHeight()) {
            // Position above the relative object
            top += relativeHeight;
        } else if (distanceFromWindowTop >= popup.getOffsetHeight()) {
            top -= popup.getOffsetHeight() - itemHeight;
        } else {// Position above the relative object and add scroll
            top += relativeHeight;
            style.setHeight(distanceToWindowBottom, Unit.PX);
            style.setOverflowY(Overflow.SCROLL);
            style.setWidth(popup.getOffsetWidth(), Unit.PX);
            //popup.getWidget().setWidth(String.valueOf(popup.getOffsetWidth()));
        }

        // Calculate left position for the popup. The computation for
        // the left position is bidi-sensitive.

        int relativeTargetOffsetWidth = relativeWidth;

        // Compute the difference between the popup's width and the
        // relative target's width
        int offsetWidthDiff = popup.getOffsetWidth() - relativeTargetOffsetWidth;

        int left;

        if (LocaleInfo.getCurrentLocale().isRTL()) { // RTL case

            int relativeTarget = relativeLeft;

            // Right-align the popup. Note that this computation is
            // valid in the case where offsetWidthDiff is negative.
            left = relativeTarget - offsetWidthDiff;

            // If the suggestion popup is not as wide as the relative object, always
            // align to the right edge of the relative object and change the width to the relative object width.
            // Otherwise, figure out whether to right-align or left-align the popup.
            if (offsetWidthDiff > 0) {

                // Make sure scrolling is taken into account, since
                // relativeTarget.getAbsoluteLeft() takes scrolling into account.
                int windowRight = Window.getClientWidth() + Window.getScrollLeft();
                int windowLeft = Window.getScrollLeft();

                // Compute the left value for the right edge of the relative target
                int relativeTargetLeftValForRightEdge = relativeTarget
                        + relativeTargetOffsetWidth;

                // Distance from the right edge of the relative object to the right edge
                // of the window
                int distanceToWindowRight = windowRight - relativeTargetLeftValForRightEdge;

                // Distance from the right edge of the relative object to the left edge of the
                // window
                int distanceFromWindowLeft = relativeTargetLeftValForRightEdge - windowLeft;

                // If there is not enough space for the overflow of the popup's
                // width to the right of the relative object and there IS enough space for the
                // overflow to the right of the relative object, then left-align the popup.
                // However, if there is not enough space on either side, stick with
                // right-alignment and add scroll.
                if (distanceFromWindowLeft < popup.getOffsetWidth()
                        && distanceToWindowRight >= offsetWidthDiff) {
                    // Align with the left edge of the relative object.
                    left = relativeTarget;
                }
            }else{
                style.setWidth(relativeWidth, Unit.PX);
            }
        } else { // LTR case

            // Left-align the popup.
            left = relativeLeft;

            // If the suggestion popup is not as wide as the relative object, always align to
            // the left edge of the relative object and change the width to the relative object width.
            // Otherwise, figure out whether to left-align or right-align the popup.
            if (offsetWidthDiff > 0) {
                // Make sure scrolling is taken into account, since
                // relativeTarget.getAbsoluteLeft() takes scrolling into account.
                int windowRight = Window.getClientWidth() + Window.getScrollLeft();
                int windowLeft = Window.getScrollLeft();

                // Distance from the left edge of the relative object to the right edge
                // of the window
                int distanceToWindowRight = windowRight - left;

                // Distance from the left edge of the relative object to the left edge of the
                // window
                int distanceFromWindowLeft = left - windowLeft;

                // If there is not enough space for the overflow of the popup's
                // width to the right of the relative object, and there is enough space for the
                // overflow to the left of the relative object, then right-align the popup.
                // However, if there is not enough space on either side, then stick with
                // left-alignment.
                if (distanceToWindowRight < popup.getOffsetWidth()
                        && distanceFromWindowLeft >= offsetWidthDiff) {
                    // Align with the right edge of the relative object.
                    left -= offsetWidthDiff;

                }
            }else{
                style.setWidth(relativeWidth, Unit.PX);
            }
        }

        popup.setPopupPosition(left, top);
    }

    public static void adjustPopupLocationToFitScreenAndShow(final PopupPanel popup, UIObject relativeObject) {
        adjustPopupLocationToFitScreenAndShow(popup,
                relativeObject.getAbsoluteLeft(),
                relativeObject.getAbsoluteTop(),
                relativeObject.getOffsetWidth(),
                relativeObject.getOffsetHeight(), 0);
    }

    public static void adjustPopupLocationToFitScreenAndShow(final PopupPanel popup, int left, int top) {
        adjustPopupLocationToFitScreenAndShow(popup, left, top, 0, 0, 0);
    }
}
