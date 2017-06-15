package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InternalNetworksPanel extends UnassignedNetworksPanel<NetworkPanel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final VerticalPanel requiredPanel = new VerticalPanel();
    private final VerticalPanel nonRequiredPanel = new VerticalPanel();
    private final List<VerticalPanel> unassignNetworksList = new ArrayList<>();
    private SimplePanel requiredTitlePanel;
    private SimplePanel nonRequiredTitlePanel;

    public InternalNetworksPanel() {
        unassignNetworksList.add(requiredPanel);
        unassignNetworksList.add(nonRequiredPanel);
    }

    @Override
    public void setStyle(final NetworkPanelsStyle style) {
        super.setStyle(style);

        stylePanel(requiredPanel);
        stylePanel(nonRequiredPanel);

        requiredTitlePanel = initTitlePanel(constants.requiredNetwork());
        nonRequiredTitlePanel = initTitlePanel(constants.nonRequiredNetwork());
    }

    private SimplePanel initTitlePanel(String title) {
        Label label = new Label(title);
        SimplePanel titlePanel = new SimplePanel(label);

        titlePanel.setStyleName(style.requiredTitlePanel());
        label.getElement().addClassName(style.requiredLabel());

        return titlePanel;
    }

    @Override
    public void addAll(List<NetworkPanel> list, boolean fadeIn) {
        requiredPanel.add(requiredTitlePanel);
        nonRequiredPanel.add(nonRequiredTitlePanel);
        for (NetworkPanel networkPanel : list) {
            LogicalNetworkModel networkModel = networkPanel.getItem();
            boolean isRequired =
                    networkModel.getNetwork().getCluster() != null && networkModel.getNetwork()
                            .getCluster()
                            .isRequired();
            if (isRequired) {
                requiredPanel.add(networkPanel);
            } else {
                nonRequiredPanel.add(networkPanel);
            }
        }
        animatedPanel.addAll(unassignNetworksList, fadeIn);
    }

    @Override
    public void clear() {
        super.clear();
        requiredPanel.clear();
        nonRequiredPanel.clear();
    }

}
