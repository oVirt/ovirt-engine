package org.ovirt.engine.ui.webadmin.widget.alert;

import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * A composite panel that contains the alert icon and the widget provided
 * by the caller, both rendered horizontally
 */
public class InLineAlertWidget extends FlowPanel {

    private final static ApplicationResources resources = AssetProvider.getResources();

    public InLineAlertWidget(Widget fromWidget) {
        Image alertIcon = new Image(resources.alertImage());
        alertIcon.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        fromWidget.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        add(alertIcon);
        add(fromWidget);
    }

}
