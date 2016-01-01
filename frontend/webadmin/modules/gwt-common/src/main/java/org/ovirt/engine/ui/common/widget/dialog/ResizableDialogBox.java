package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DialogBox;

public class ResizableDialogBox extends DialogBox {

    private boolean resizeSupportEnabled;
    private boolean dragging;
    private boolean rightEdge;
    private boolean bottomEdge;
    private int minWidth;
    private int minHeight;

    private static int EDGE_SIZE = 3;
    private static int EDGE_THRESHOLD = 10;

    private Style parentElementStyle;
    private static String STYLE_RESIZE_POSTFIX = "resize"; //$NON-NLS-1$

    public ResizableDialogBox() {
        enableResizeSupport(false);
    }

    @Override
    public void onBrowserEvent(Event event) {
        // When resize support is disabled, fall-back to default browser behavior
        if (!resizeSupportEnabled) {
            super.onBrowserEvent(event);
            return;
        }

        // Set the default dialog's dimensions as minimal dimensions
        if (minWidth == 0 || minHeight == 0) {
            minWidth = getOffsetWidth();
            minHeight = getOffsetHeight();
        }

        // While dragging dialog's edge is on, disable default event behavior
        if (dragging) {
            event.preventDefault();
        }
        else {
            super.onBrowserEvent(event);
        }

        final int eventType = DOM.eventGetType(event);
        if (Event.ONMOUSEMOVE == eventType) {
            if (dragging) {
                // Get cursor's position
                int cursorX = event.getClientX();
                int cursorY = event.getClientY();

                // Calculates dialog's new dimensions
                int newWidth = cursorX - DOM.getAbsoluteLeft(getElement()) + EDGE_SIZE;
                int newHeight = cursorY - DOM.getAbsoluteTop(getElement()) + EDGE_SIZE;

                updateDialogDimensions(newWidth, newHeight);
            }
            else {
                updateResizeCursor(event);
            }
        } else if (Event.ONMOUSEDOWN == eventType) {
            if (updateResizeCursor(event) && !dragging) {
                event.preventDefault();
                dragging = true;
                DOM.setCapture(getElement());
            }
        } else if (Event.ONMOUSEUP == eventType) {
            if (dragging) {
                dragging = false;
                DOM.releaseCapture(getElement());
            }
        } else if (Event.ONMOUSEOUT == eventType) {
            if (!dragging) {
                updateResizeCursor(event);
            }
        }
    }

    /**
     * Enable/Disable dialog's resize support
     */
    public void enableResizeSupport(boolean enabled) {
        this.resizeSupportEnabled = enabled;

        Element dialogBottomRight = getElementByTagAndClassName("td", "dialogBottomRight"); //$NON-NLS-1$ //$NON-NLS-2$
        assert dialogBottomRight != null;

        if (enabled) {
            dialogBottomRight.addClassName(STYLE_RESIZE_POSTFIX);
        }
        else {
            dialogBottomRight.removeClassName(STYLE_RESIZE_POSTFIX);
        }
    }

    /**
     * Update dialog's dimensions according to the sepcified width/height
     */
    private void updateDialogDimensions(int newWidth, int newHeight) {
        if (newWidth <= minWidth) {
            newWidth = minWidth;
        }

        if (newHeight <= minHeight) {
            newHeight = minHeight;
        }

        if (rightEdge && bottomEdge) {
            setWidth(newWidth + "px"); //$NON-NLS-1$
            setHeight(newHeight + "px"); //$NON-NLS-1$
        }
        else if (rightEdge) {
            setWidth(newWidth + "px"); //$NON-NLS-1$
        }
        else if (bottomEdge) {
            setHeight(newHeight + "px"); //$NON-NLS-1$
        }
    }

    /**
     * Detect dialog's edges and update mouse cursor according to hovered dialog edge
     */
    private boolean updateResizeCursor(Event event) {
        int cursorX = event.getClientX();
        int initialX = getAbsoluteLeft();
        int width = getOffsetWidth();

        int cursorY = event.getClientY();
        int initialY = getAbsoluteTop();
        int height = getOffsetHeight();

        rightEdge = (initialX + width - EDGE_THRESHOLD) < cursorX && cursorX < (initialX + width);
        bottomEdge = (initialY + height - EDGE_THRESHOLD) < cursorY && cursorY < (initialY + height);

        String cursor = bottomEdge && rightEdge ? "se-resize" : //$NON-NLS-1$
                rightEdge ? "e-resize" : bottomEdge ? "n-resize" : "default"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        if (parentElementStyle == null) {
            parentElementStyle = getElement().getParentElement().getStyle();
        }
        getElement().getParentElement().getStyle().setProperty("cursor", cursor); //$NON-NLS-1$

        return rightEdge || bottomEdge;
    }

    private Element getElementByTagAndClassName(String tagName, String className) {
        NodeList<Element> elements = getElement().getElementsByTagName(tagName);

        for (int i = 0; i < elements.getLength(); i++) {
            Element element = elements.getItem(i);
            if (element.getClassName().contains(className)) {
                return element;
            }
        }

        return null;
    }

    @Override
    public void hide() {
        super.hide();
        // reset mouse cursor to default, when the dialog closes
        if (resizeSupportEnabled
                && parentElementStyle != null
                && !Cursor.DEFAULT.getCssName().equals(parentElementStyle.getCursor())) {
            parentElementStyle.setCursor(Cursor.DEFAULT);
        }
    }
}
