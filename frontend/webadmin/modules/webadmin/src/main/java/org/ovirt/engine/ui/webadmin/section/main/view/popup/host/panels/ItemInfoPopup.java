package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;

public class ItemInfoPopup extends DecoratedPopupPanel {

    private final FlexTable contents = new FlexTable();
    private static final EnumRenderer<NetworkBootProtocol> RENDERER = new EnumRenderer<NetworkBootProtocol>();

    private final static ApplicationTemplates templates = AssetProvider.getTemplates();
    private final static ApplicationResources resources = AssetProvider.getResources();
    private final static ApplicationConstants constants = AssetProvider.getConstants();
    private final static ApplicationMessages messages = AssetProvider.getMessages();

    SafeHtml mgmtNetworkImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.mgmtNetwork())
            .getHTML());
    SafeHtml vmImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkVm()).getHTML());
    SafeHtml monitorImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkMonitor())
            .getHTML());
    SafeHtml migrationImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.migrationNetwork())
                    .getHTML());
    SafeHtml glusterNwImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.glusterNetwork())
                    .getHTML());
    SafeHtml unknownImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.questionMarkImage()).getHTML());
    SafeHtml notInSyncImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkNotSyncImage()).getHTML());
    SafeHtml alertImage =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.alertImage()).getHTML());
    private static int defaultMtu = (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.DefaultMTU);

    public ItemInfoPopup() {
        super(true);
        contents.setCellPadding(5);

        setWidget(contents);
        getElement().getStyle().setZIndex(1);
    }

    public String getTooltipContent(NetworkItemModel<?> item, NetworkItemPanel panel) {
        if (item instanceof LogicalNetworkModel) {
            showNetwork((LogicalNetworkModel) item);
        } else if (item instanceof NetworkInterfaceModel) {
            showNic((NetworkInterfaceModel) item);
        } else if (item instanceof NetworkLabelModel){
            return null;
        }

        return this.getElement().getInnerHTML();

    }

    private void addRow(String label, String value) {
        int rowCount = contents.insertRow(contents.getRowCount());
        contents.setText(rowCount, 0, label + ": " + value); //$NON-NLS-1$
    }

    private void addRow(SafeHtml value) {
        int rowCount = contents.insertRow(contents.getRowCount());
        contents.setHTML(rowCount, 0, value);
    }

    private void showNetwork(LogicalNetworkModel networkModel) {
        contents.removeAllRows();
        Network entity = networkModel.getNetwork();
        addRow(templates.titleSetupNetworkTooltip(networkModel.getName()));

        // Not managed
        if (!networkModel.isManaged()) {
            addRow(templates.imageTextSetupNetwork(unknownImage, constants.unmanagedNetworkItemInfo()));
            addRow(SafeHtmlUtils.fromString(constants.unmanagedNetworkDescriptionItemInfo()));
        }
        else {
            if (networkModel.getErrorMessage() != null) {
                addRow(templates.imageTextSetupNetwork(alertImage, templates.maxWidthNteworkItemPopup(networkModel.getErrorMessage())));
            }
            // Description
            if (entity.getDescription() != null && !entity.getDescription().trim().equals("")) { //$NON-NLS-1$
                addRow(SafeHtmlUtils.fromString(entity.getDescription()));
            }
            // Not in sync
            if (!networkModel.isInSync()) {
                addRow(templates.imageTextSetupNetwork(notInSyncImage, constants.networkNotInSync()));
            }
        }

        boolean isDisplay = false;
        boolean isMigration = false;
        boolean isGlusterNw = false;

        if (entity.getCluster() != null) {
            isDisplay = entity.getCluster().isDisplay();
            isMigration = entity.getCluster().isMigration();
            isGlusterNw = entity.getCluster().isGluster();
        }
        // Usages
        if (networkModel.isManagement() || isDisplay || entity.isVmNetwork() || isMigration || isGlusterNw) {

            addRow(SafeHtmlUtils.fromString(constants.usageItemInfo() + ":")); //$NON-NLS-1$
            if (networkModel.isManagement()) {
                addRow(templates.imageTextSetupNetworkUsage(mgmtNetworkImage, constants.managementItemInfo()));
            }

            if (isDisplay) {
                addRow(templates.imageTextSetupNetworkUsage(monitorImage, constants.displayItemInfo()));
            }

            if (entity.isVmNetwork()) {
                addRow(templates.imageTextSetupNetworkUsage(vmImage, constants.vmItemInfo()));
            }

            if (isMigration) {
                addRow(templates.imageTextSetupNetworkUsage(migrationImage, constants.migrationItemInfo()));
            }

            if (isGlusterNw) {
                addRow(templates.imageTextSetupNetworkUsage(glusterNwImage, constants.glusterNwItemInfo()));
            }

        }

        // Mtu
        if (!entity.isExternal()) {
            addRow(constants.mtuItemInfo(),
                    entity.getMtu() == 0 ? messages.defaultMtu(defaultMtu) : String.valueOf(entity.getMtu()));
        }
    }

    private void showNic(NetworkInterfaceModel nic) {
        contents.removeAllRows();
        VdsNetworkInterface entity = nic.getIface();
        NetworkBootProtocol bootProtocol = entity.getBootProtocol();
        addRow(templates.titleSetupNetworkTooltip(nic.getName()));
        addRow(constants.bootProtocolItemInfo(), RENDERER.render(bootProtocol));
        if (bootProtocol == NetworkBootProtocol.STATIC_IP) {
            addRow(constants.addressItemInfo(), entity.getAddress());
            addRow(constants.subnetItemInfo(), entity.getSubnet());
            addRow(constants.gatewayItemInfo(), entity.getGateway());
        }
        if (nic instanceof BondNetworkInterfaceModel) {
            addRow(constants.bondOptionsItemInfo(), entity.getBondOptions());
        }
    }
}
