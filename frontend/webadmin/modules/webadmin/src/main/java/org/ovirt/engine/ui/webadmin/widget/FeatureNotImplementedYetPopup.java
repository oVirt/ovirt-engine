package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FeatureNotImplementedYetPopup extends DecoratedPopupPanel {
    public FeatureNotImplementedYetPopup(Widget widget, Boolean availableInUserPortal) {
        super(true);
        ApplicationConstants constants = ClientGinjectorProvider.instance().getApplicationConstants();

        if (availableInUserPortal)
            setWidget(new Label(constants.featureNotImplementedButAvailInUserPortalMessage()));
        else
            setWidget(new Label(constants.featureNotImplementedMessage()));

        setWidth("200px");
        int left = widget.getAbsoluteLeft() + 10;
        int top = widget.getAbsoluteTop() + 10;
        setPopupPosition(left, top);
    }
}
