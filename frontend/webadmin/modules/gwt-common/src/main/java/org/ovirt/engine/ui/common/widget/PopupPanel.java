package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.utils.PopupUtils;

import com.google.gwt.user.client.ui.UIObject;

public class PopupPanel extends com.google.gwt.user.client.ui.PopupPanel {

    public PopupPanel() {
        super();
    }

    public PopupPanel(boolean autoHide) {
        super(autoHide);
    }

    public PopupPanel(boolean autoHide, boolean modal) {
        super(autoHide, modal);
    }

    public void showAndFitToScreen(int left, int top) {
        // The show must come first in order to get the correct offsetWidth and offsetHeight.
        // Until we show the element it is not connected to the DOM and its width and height are 0.
        PopupUtils.adjustPopupLocationToFitScreenAndShow(this, left, top);
    }

    public void showRelativeToAndFitToScreen(final UIObject relativeObject) {
        PopupUtils.adjustPopupLocationToFitScreenAndShow(this, relativeObject);
    }
}
