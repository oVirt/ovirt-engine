package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;

public class ExternalNetworksPanel extends UnassignedNetworksPanel {

    private VerticalPanel externalNetworksPanel = new VerticalPanel();

    @Override
    public void setStyle(NetworkPanelsStyle style) {
        super.setStyle(style);
        stylePanel(externalNetworksPanel);
    }

    @Override
    public void addAll(List<NetworkPanel> list, boolean fadeIn) {
        for (NetworkPanel networkPanel : list) {
            externalNetworksPanel.add(networkPanel);
        }
        animatedPanel.addAll(Collections.singletonList(externalNetworksPanel), fadeIn);
    }

    @Override
    public void clear() {
        super.clear();
        externalNetworksPanel.clear();
    }

}
