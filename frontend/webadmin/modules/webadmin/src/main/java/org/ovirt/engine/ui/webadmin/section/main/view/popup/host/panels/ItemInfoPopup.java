package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;

public class ItemInfoPopup extends DecoratedPopupPanel {

    private final FlexTable contents = new FlexTable();
    private static final EnumRenderer<NetworkBootProtocol> RENDERER = new EnumRenderer<NetworkBootProtocol>();

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
        contents.setHTML(rowCount, 0, "<B>" + label + ":</B>");
        contents.setText(rowCount, 1, value);
    }

    private void showNetwork(LogicalNetworkModel networkModel) {
        contents.removeAllRows();
        network entity = networkModel.getEntity();
        addRow("Name", networkModel.getName());
        addRow("Description", entity.getdescription());
        addRow("VLan", networkModel.hasVlan() ? networkModel.getVlanId() + "" : "NA");
        addRow("Display", entity.getis_display() ? "Yes" : "No");
        addRow("Management", networkModel.isManagement() ? "Yes" : "No");
    }

    private void showNic(NetworkInterfaceModel nic) {
        contents.removeAllRows();
        VdsNetworkInterface entity = nic.getEntity();
        NetworkBootProtocol bootProtocol = entity.getBootProtocol();
        addRow("Name", nic.getName());
        addRow("Boot Protocol", RENDERER.render(bootProtocol));
        if (bootProtocol == NetworkBootProtocol.StaticIp) {
            addRow("Address", entity.getAddress());
            addRow("Subnet", entity.getSubnet());
            if (entity.getIsManagement()) {
                addRow("Gateway", entity.getGateway());
            }
        }
        if (nic instanceof BondNetworkInterfaceModel) {
            addRow("Bond Options", entity.getBondOptions());
        }
    }
}
