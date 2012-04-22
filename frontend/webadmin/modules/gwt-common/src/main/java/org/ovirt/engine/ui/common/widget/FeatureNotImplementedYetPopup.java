package org.ovirt.engine.ui.common.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FeatureNotImplementedYetPopup extends DecoratedPopupPanel {

    interface PopupConstants extends Constants {

        @DefaultStringValue("This feature is not implemented in this version.")
        String featureNotImplementedMessage();

        @DefaultStringValue("This feature is not implemented but available in UserPortal for users assigned with PowerUser role.")
        String featureNotImplementedButAvailInUserPortalMessage();

    }

    private static final PopupConstants CONSTANTS = GWT.create(PopupConstants.class);

    public FeatureNotImplementedYetPopup(Widget target, boolean availableInUserPortal) {
        super(true);

        if (availableInUserPortal) {
            setWidget(new Label(CONSTANTS.featureNotImplementedButAvailInUserPortalMessage()));
        } else {
            setWidget(new Label(CONSTANTS.featureNotImplementedMessage()));
        }

        setWidth("200px"); //$NON-NLS-1$
        int left = target.getAbsoluteLeft() + 10;
        int top = target.getAbsoluteTop() + 10;
        setPopupPosition(left, top);
    }

}
