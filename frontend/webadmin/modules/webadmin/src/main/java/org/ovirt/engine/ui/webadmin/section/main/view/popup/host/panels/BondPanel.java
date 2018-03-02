package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BondPanel extends NicPanel {

    private List<NetworkItemPanel> nicPanels;

    private static final ApplicationResources resources = AssetProvider.getResources();

    public BondPanel(BondNetworkInterfaceModel item, NetworkPanelsStyle style) {
        super(item, style, true);
        actionButton.setStyleName(style.actionButtonBond());
        actionButton.setStyleName("bp_actionButton_pfly_fix"); //$NON-NLS-1$
    }

    @Override
    protected Widget getContents() {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.addStyleName("ts5"); //$NON-NLS-1$
        vPanel.setWidth("100%"); //$NON-NLS-1$

        Grid titleRow = new Grid(1, 3);
        titleRow.addStyleName("ts3"); //$NON-NLS-1$

        ColumnFormatter columnFormatter = titleRow.getColumnFormatter();
        columnFormatter.setWidth(0, "30px"); //$NON-NLS-1$
        columnFormatter.setWidth(1, "100%"); //$NON-NLS-1$
        titleRow.setWidth("100%"); //$NON-NLS-1$
        titleRow.setHeight("27px"); //$NON-NLS-1$

        Label titleLabel = new Label(item.getName());
        titleLabel.setHeight("100%"); //$NON-NLS-1$
        Image bondImage = new Image(resources.bond());

        titleRow.setWidget(0, 0, bondImage);
        titleRow.setWidget(0, 1, titleLabel);
        titleRow.setWidget(0, 2, actionButton);
        titleRow.addStyleName("ts3"); //$NON-NLS-1$
        titleRow.setCellPadding(3);
        vPanel.add(titleRow);

        getElement().addClassName(style.bondPanel());
        List<NetworkInterfaceModel> bonded = ((BondNetworkInterfaceModel) item).getSlaves();
        Collections.sort(bonded);

        nicPanels = new ArrayList<>();
        for (NetworkInterfaceModel networkInterfaceModel : bonded) {
            NicPanel nicPanel = new NicPanel(networkInterfaceModel, style);
            nicPanel.parentPanel = this;
            if (!networkInterfaceModel.isSriovEnabled()) {
                nicPanel.actionButton.setVisible(false);
            }
            vPanel.add(nicPanel);
            nicPanels.add(nicPanel);
        }

        return vPanel;
    }

    @Override
    public void redrawTooltip() {
        super.redrawTooltip();
        nicPanels.forEach(NetworkItemPanel::redrawTooltip);
    }

    @Override
    protected void onAction() {
        item.edit();
    }

}
