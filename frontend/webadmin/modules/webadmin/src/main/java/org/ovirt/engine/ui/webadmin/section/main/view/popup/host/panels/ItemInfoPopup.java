package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;

public class ItemInfoPopup extends DecoratedPopupPanel {

    private final FlexTable contents = new FlexTable();
    private static final EnumRenderer<NetworkBootProtocol> RENDERER = new EnumRenderer<NetworkBootProtocol>();
    private ApplicationConstants constants = ClientGinjectorProvider.instance().getApplicationConstants();

    public ItemInfoPopup() {
        super(true);
        contents.setCellPadding(5);
        setWidget(contents);
        getElement().getStyle().setZIndex(1);
    }

    public void showItem(NetworkItemModel<?> item, NetworkItemPanel panel) {
        contents.clear();
        if (item instanceof LogicalNetworkModel) {
            showNetwork((LogicalNetworkModel) item);
        } else if (item instanceof NetworkInterfaceModel) {
            showNic((NetworkInterfaceModel) item);
        }
        showRelativeTo(panel);
    }

    private void addRow(String label, String value) {
        int rowCount = contents.insertRow(contents.getRowCount());
        contents.setHTML(rowCount, 0, "<B>" + label + ":</B>"); //$NON-NLS-1$ //$NON-NLS-2$
        contents.setText(rowCount, 1, value);
    }

    private void showNetwork(LogicalNetworkModel networkModel) {
        contents.removeAllRows();
        Network entity = networkModel.getEntity();
        addRow(constants.nameItemInfo(), networkModel.getName());
        addRow(constants.descriptionItemInfo(), entity.getdescription());
        addRow(constants.VLanItemInfo(), networkModel.hasVlan() ? networkModel.getVlanId() + "" : "NA"); //$NON-NLS-1$ //$NON-NLS-2$
        addRow(constants.displayItemInfo(), entity.getis_display() ? "Yes" : "No"); //$NON-NLS-1$ //$NON-NLS-2$
        addRow(constants.managementItemInfo(), networkModel.isManagement() ? "Yes" : "No"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void showNic(NetworkInterfaceModel nic) {
        contents.removeAllRows();
        VdsNetworkInterface entity = nic.getEntity();
        NetworkBootProtocol bootProtocol = entity.getBootProtocol();
        addRow(constants.nameItemInfo(), nic.getName());
        addRow(constants.bootProtocolItemInfo(), RENDERER.render(bootProtocol));
        if (bootProtocol == NetworkBootProtocol.StaticIp) {
            addRow(constants.addressItemInfo(), entity.getAddress());
            addRow(constants.subnetItemInfo(), entity.getSubnet());
            if (entity.getIsManagement()) {
                addRow(constants.gatewayItemInfo(), entity.getGateway());
            }
        }
        if (nic instanceof BondNetworkInterfaceModel) {
            addRow(constants.bondOptionsItemInfo(), entity.getBondOptions());
        }
    }
}
