package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class InternalNetworkPanel extends NetworkPanel {

    private static final ApplicationResources resources = AssetProvider.getResources();

    public InternalNetworkPanel(LogicalNetworkModel item, NetworkPanelsStyle style) {
        this(item, style, true);
    }

    public InternalNetworkPanel(LogicalNetworkModel item, NetworkPanelsStyle style, boolean draggable) {
        super(item, style, draggable);
        getElement().addClassName(style.networkPanel());
        getElement().addClassName(item.hasVlan() ? style.networkPanelWithVlan() : style.networkPanelWithoutVlan());
    }

    @Override
    protected ImageResource getStatusImage() {
        NetworkStatus netStatus = item.getStatus();

        if (netStatus == NetworkStatus.OPERATIONAL) {
            return resources.upImage();
        } else if (netStatus == NetworkStatus.NON_OPERATIONAL) {
            return resources.downImage();
        } else {
            return resources.questionMarkImage();
        }
    }

}
