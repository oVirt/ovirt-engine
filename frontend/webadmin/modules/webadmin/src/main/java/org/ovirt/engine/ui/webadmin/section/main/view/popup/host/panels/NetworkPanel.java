package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class NetworkPanel extends NetworkItemPanel {

    public NetworkPanel(LogicalNetworkModel item, NetworkPanelsStyle style) {
        super(item, style, true);
        getElement().addClassName(style.networkPanel());
        if (item.isManagement()) {
            getElement().addClassName(style.mgmtNetwork());
        }

    }

    @Override
    protected Widget getContents() {
        LogicalNetworkModel network = (LogicalNetworkModel) item;

        Image mgmtNetworkImage;
        Image vmImage;
        Image monitorImage;
        Image notSyncImage;

        if (network.getEntity().getCluster() == null){
            monitorImage = new Image(resources.questionMarkImage());
            mgmtNetworkImage = new Image(resources.empty());
            vmImage = new Image(resources.empty());
            notSyncImage = new Image(resources.empty());
        }else{

            monitorImage = new Image(network.getEntity().getCluster().getis_display() ? resources.networkMonitor() : resources.empty());
            mgmtNetworkImage = new Image(network.isManagement() ? resources.mgmtNetwork() : resources.empty());
            vmImage = new Image(network.getEntity().isVmNetwork() ? resources.networkVm() : resources.empty());
            notSyncImage = new Image(!network.isInSync() ? resources.networkNotSyncImage() : resources.empty());

            if (network.isManagement()){
                mgmtNetworkImage.setStylePrimaryName(style.networkImageBorder());
            }

            if (network.getEntity().isVmNetwork()){
                vmImage.setStylePrimaryName(style.networkImageBorder());
            }

            if (network.getEntity().getCluster().getis_display()){
                monitorImage.setStylePrimaryName(style.networkImageBorder());
            }

            if (!network.isInSync()){
                notSyncImage.setStylePrimaryName(style.networkImageBorder());
            }
        }

        Grid rowPanel = new Grid(1, 8);
        rowPanel.setCellSpacing(3);
        rowPanel.setWidth("100%"); //$NON-NLS-1$
        rowPanel.setHeight("100%"); //$NON-NLS-1$

        ColumnFormatter columnFormatter = rowPanel.getColumnFormatter();
        columnFormatter.setWidth(0, "5px"); //$NON-NLS-1$
        columnFormatter.setWidth(1, "20px"); //$NON-NLS-1$
        columnFormatter.setWidth(2, "100%"); //$NON-NLS-1$

        rowPanel.setWidget(0, 0, dragImage);

        ImageResource statusImage = getStatusImage();
        if (statusImage != null) {
            rowPanel.setWidget(0, 1, new Image(statusImage));
        }
        Label titleLabel = new Label(getItemTitle());
        rowPanel.setWidget(0, 2, titleLabel);
        rowPanel.setWidget(0, 3, actionButton);
        rowPanel.setWidget(0, 4, mgmtNetworkImage);
        rowPanel.setWidget(0, 5, monitorImage);
        rowPanel.setWidget(0, 6, vmImage);
        rowPanel.setWidget(0, 7, notSyncImage);
        return rowPanel;
    }

    protected ImageResource getStatusImage() {
        NetworkStatus netStatus = ((LogicalNetworkModel) item).getStatus();

        if (netStatus == NetworkStatus.Operational){
            return resources.upImage();
        } else if (netStatus == NetworkStatus.NonOperational){
            return resources.downImage();
        }else{
            return resources.questionMarkImage();
        }
    }

    private String getItemTitle() {
        LogicalNetworkModel networkModel = (LogicalNetworkModel) item;
        if (networkModel.hasVlan()) {
            return networkModel.getName() + " (vlan " + networkModel.getVlanId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return item.getName();
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
        LogicalNetworkModel network = (LogicalNetworkModel) item;
        if (network!=null && network.getAttachedToNic()!=null) {
            actionButton.setVisible(true);
        }
    }

}
