package org.ovirt.engine.ui.webadmin.widget.alert;

import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * A composite panel that contains the alert icon and the widget provided
 * by the caller, both rendered horizontally
 */
public class InLineAlertWidget extends FlowPanel {
    private static final ApplicationResources resources = AssetProvider.getResources();

    public static enum AlertType {
        ALERT(resources.alertImage()),
        UPDATE_AVAILABLE(resources.updateAvailableImage());

        private ImageResource imageResource;

        private AlertType(ImageResource imageResource) {
            this.imageResource = imageResource;
        }

        public ImageResource getImageResource() {
            return imageResource;
        }
    }

    public InLineAlertWidget(Widget fromWidget) {
        this(fromWidget, AlertType.ALERT);
    }

    public InLineAlertWidget(Widget fromWidget, AlertType type) {
        Image alertIcon = new Image(type.getImageResource());
        alertIcon.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        fromWidget.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        add(alertIcon);
        add(fromWidget);
    }
}
