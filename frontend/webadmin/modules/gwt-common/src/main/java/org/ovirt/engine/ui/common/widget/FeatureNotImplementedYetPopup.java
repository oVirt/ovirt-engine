package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FeatureNotImplementedYetPopup extends DecoratedPopupPanel {

    public FeatureNotImplementedYetPopup(Widget target,
            boolean availableInUserPortal, CommonApplicationConstants constants) {
        super(true);

        if (availableInUserPortal) {
            setWidget(new Label(constants.featureNotImplementedButAvailInUserPortalMessage()));
        } else {
            setWidget(new Label(constants.featureNotImplementedMessage()));
        }

        setWidth("200px");
        int left = target.getAbsoluteLeft() + 10;
        int top = target.getAbsoluteTop() + 10;
        setPopupPosition(left, top);
    }

}
