package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;

public class SimpleNetworkItemsPanel<T extends NetworkItemPanel<?>> extends UnassignedNetworksPanel<T> {

    private VerticalPanel itemsPanel = new VerticalPanel();

    @Override
    public void setStyle(NetworkPanelsStyle style) {
        super.setStyle(style);
        stylePanel(itemsPanel);
    }

    @Override
    public void addAll(List<T> list, boolean fadeIn) {
        for (T panel : list) {
            itemsPanel.add(panel);
        }
        animatedPanel.addAll(Collections.singletonList(itemsPanel), fadeIn);
    }

    @Override
    public void clear() {
        super.clear();
        itemsPanel.clear();
    }

}
