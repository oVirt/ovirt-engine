package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkCommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperationFactory.OperationMap;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;

public abstract class NetworkPanel extends NetworkItemPanel {

    public NetworkPanel(LogicalNetworkModel item, NetworkPanelsStyle style, boolean draggable) {
        super(item, style, draggable);
        if (item.isManagement()) {
            getElement().addClassName(style.mgmtNetwork());
        }

        // Not managed
        if (!item.isManaged()) {
            actionButton.getUpFace().setImage(new Image(resources.butEraseNetHover()));
            actionButton.getDownFace().setImage(new Image(resources.butEraseNetMousedown()));
        }
        actionButton.setStyleName(style.actionButtonNetwork());
        actionButton.addStyleName("np_actionButton_pfly_fix"); //$NON-NLS-1$
    }

    @Override
    protected Widget getContents() {
        LogicalNetworkModel network = (LogicalNetworkModel) item;

        Image mgmtNetworkImage;
        Image vmImage;
        Image monitorImage;
        Image migrationImage;
        Image notSyncImage;
        Image alertImage;

        if (!network.isManaged()) {
            monitorImage = null;
            mgmtNetworkImage = null;
            vmImage = null;
            migrationImage = null;
            notSyncImage = null;
            alertImage = null;
        } else {
            monitorImage = network.getEntity().getCluster().isDisplay() ?
                    new Image(resources.networkMonitor()) : null;
            mgmtNetworkImage = network.isManagement() ? new Image(resources.mgmtNetwork()) : null;
            vmImage = network.getEntity().isVmNetwork() ? new Image(resources.networkVm()) : null;
            migrationImage = network.getEntity().getCluster().isMigration() ?
                    new Image(resources.migrationNetwork()) : null;
            notSyncImage = !network.isInSync() ? new Image(resources.networkNotSyncImage()) : null;
            alertImage = network.getErrorMessage() != null ? new Image(resources.alertImage()) : null;

            if (network.isManagement()) {
                mgmtNetworkImage.setStylePrimaryName(style.networkImageBorder());
            }

            if (network.getEntity().isVmNetwork()) {
                vmImage.setStylePrimaryName(style.networkImageBorder());
            }

            if (network.getEntity().getCluster().isDisplay()) {
                monitorImage.setStylePrimaryName(style.networkImageBorder());
            }

            if (network.getEntity().getCluster().isMigration()) {
                migrationImage.setStylePrimaryName(style.networkImageBorder());
            }

            if (!network.isInSync()) {
                notSyncImage.setStylePrimaryName(style.networkImageBorder());
            }
        }

        actionButton.setVisible(network.getAttachedToNic() != null
                && (network.isManaged() || !network.isAttachedViaLabel()));

        Grid rowPanel = new Grid(1, 9);
        rowPanel.setCellSpacing(0);
        rowPanel.setWidth("100%"); //$NON-NLS-1$
        rowPanel.setHeight("100%"); //$NON-NLS-1$

        ColumnFormatter columnFormatter = rowPanel.getColumnFormatter();
        columnFormatter.setWidth(0, "5px"); //$NON-NLS-1$
        columnFormatter.setWidth(1, "20px"); //$NON-NLS-1$
        columnFormatter.setWidth(2, "100%"); //$NON-NLS-1$

        rowPanel.setWidget(0, 0, dragImage);

        Panel statusPanel = new HorizontalPanel();
        rowPanel.setWidget(0, 1, statusPanel);
        if (alertImage != null) {
            statusPanel.add(alertImage);
        }

        ImageResource statusImage = getStatusImage();
        if (statusImage != null) {
            statusPanel.add(new Image(statusImage));
        }
        Label titleLabel = new Label(getItemTitle());
        rowPanel.setWidget(0, 2, titleLabel);
        rowPanel.setWidget(0, 3, mgmtNetworkImage);
        rowPanel.setWidget(0, 4, monitorImage);
        rowPanel.setWidget(0, 5, vmImage);
        rowPanel.setWidget(0, 6, migrationImage);
        rowPanel.setWidget(0, 7, notSyncImage);
        rowPanel.setWidget(0, 8, actionButton);
        return rowPanel;
    }

    protected abstract ImageResource getStatusImage();

    private String getItemTitle() {
        LogicalNetworkModel networkModel = (LogicalNetworkModel) item;
        if (networkModel.hasVlan()) {
            return messages.vlanNetwork(networkModel.getName(), String.valueOf(networkModel.getVlanId()));
        }
        return item.getName();
    }

    @Override
    protected void onAction() {
        LogicalNetworkModel network = (LogicalNetworkModel) item;
        if (network.isManaged()) {
            item.edit();
        } else {
            OperationMap operationMap = item.getSetupModel().commandsFor(item);
            final NetworkCommand detach = operationMap.get(NetworkOperation.REMOVE_UNMANAGED_NETWORK).get(0);
            item.getSetupModel().onOperation(NetworkOperation.REMOVE_UNMANAGED_NETWORK, detach);
        }
    }

}
