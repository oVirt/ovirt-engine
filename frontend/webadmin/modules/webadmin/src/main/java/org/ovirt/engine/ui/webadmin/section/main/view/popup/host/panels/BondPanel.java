package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BondPanel extends NicPanel {

    public BondPanel(BondNetworkInterfaceModel item, NetworkPanelsStyle style) {
        super(item, style, false);
    }

    @Override
    protected Widget getContents() {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setSpacing(5);
        vPanel.setWidth("100%"); //$NON-NLS-1$

        Grid titleRow = new Grid(1, 3);
        titleRow.setCellSpacing(3);

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
        titleRow.setCellSpacing(3);
        titleRow.setCellPadding(3);
        vPanel.add(titleRow);

        getElement().addClassName(style.bondPanel());
        List<NetworkInterfaceModel> bonded = ((BondNetworkInterfaceModel) item).getBonded();
        Collections.sort(bonded);

        for (NetworkInterfaceModel networkInterfaceModel : bonded) {
            NicPanel nicPanel = new NicPanel(networkInterfaceModel, style);
            nicPanel.parentPanel = this;
            vPanel.add(nicPanel);
        }

        return vPanel;
    }

    @Override
    protected void onAction() {
        item.edit();
    }

    @Override
    protected void onMouseOut() {
        super.onMouseOut();
        actionButton.setVisible(false);
    }

    @Override
    protected void onMouseOver() {
        super.onMouseOver();
        actionButton.setVisible(true);
    }

}
