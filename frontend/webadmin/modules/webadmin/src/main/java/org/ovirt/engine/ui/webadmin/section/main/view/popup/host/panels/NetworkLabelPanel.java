package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.common.widget.label.LabelWithTextTruncation;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class NetworkLabelPanel extends NetworkItemPanel<NetworkLabelModel> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    public NetworkLabelPanel(NetworkLabelModel item, NetworkPanelsStyle style) {
        super(item, style, true);
    }

    protected void addClassName() {
        getElement().addClassName(style.labelPanel());
    }

    @Override
    protected Widget getContents() {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.addStyleName("ts5"); //$NON-NLS-1$
        vPanel.setWidth("100%"); //$NON-NLS-1$

        Grid titleRow = new Grid(1, 2);
        titleRow.addStyleName("ts3"); //$NON-NLS-1$

        ColumnFormatter columnFormatter = titleRow.getColumnFormatter();
        columnFormatter.setWidth(0, "30px"); //$NON-NLS-1$
        columnFormatter.setWidth(1, "100%"); //$NON-NLS-1$
        titleRow.setWidth("100%"); //$NON-NLS-1$
        titleRow.setHeight("27px"); //$NON-NLS-1$

        LabelWithTextTruncation titleLabel = new LabelWithTextTruncation(item.getName());
        titleLabel.setWidth("185px"); //$NON-NLS-1$
        titleLabel.setHeight("100%"); //$NON-NLS-1$
        Image labelImage = new Image(resources.tagImage());

        titleRow.setWidget(0, 0, labelImage);
        titleRow.setWidget(0, 1, titleLabel);
        titleRow.addStyleName("ts3"); //$NON-NLS-1$
        titleRow.addStyleName("tp3"); //$NON-NLS-1$
        vPanel.add(titleRow);

        addClassName();

        List<LogicalNetworkModel> networks = item.getNetworks();
        Collections.sort(networks);

        for (LogicalNetworkModel network : networks) {
            InternalNetworkPanel networkPanel = new InternalNetworkPanel(network, style, false);
            networkPanel.parentPanel = this;
            vPanel.add(networkPanel);
        }

        return vPanel;
    }

    @Override
    protected void onAction() {
        // Do nothing
    }

    public static class NewNetworkLabelPanel extends NetworkLabelPanel {

        public NewNetworkLabelPanel(NetworkLabelModel item, NetworkPanelsStyle style) {
            super(item, style);
        }

        @Override
        protected void addClassName() {
            super.addClassName();
            getElement().addClassName(style.newLabelPanel());
        }
    }

}
