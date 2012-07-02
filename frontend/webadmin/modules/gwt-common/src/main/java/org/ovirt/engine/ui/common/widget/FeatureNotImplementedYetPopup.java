package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FeatureNotImplementedYetPopup extends DecoratedPopupPanel {

    private static final CommonApplicationConstants CONSTANTS = GWT.create(CommonApplicationConstants.class);

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
