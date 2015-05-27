package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NetworkLabelModel extends NetworkItemModel<NetworkStatus> {

    private final String label;
    private final List<LogicalNetworkModel> labelNetworks;
    private NetworkInterfaceModel iface;

    public NetworkLabelModel(String label, HostSetupNetworksModel setupModel) {
        super(setupModel);
        this.label = label;
        labelNetworks = new ArrayList<>();
    }

    @Override
    public String getName() {
        return label;
    }

    @Override
    public NetworkStatus getStatus() {
        return null;
    }

    @Override
    public String getType() {
        return HostSetupNetworksModel.LABEL;
    }

    public List<LogicalNetworkModel> getNetworks() {
        return labelNetworks;
    }

    public NetworkInterfaceModel getInterface() {
        return iface;
    }

    public void setInterface(NetworkInterfaceModel iface) {
        this.iface = iface;
    }

    public boolean isAttached() {
        return iface != null;
    }

    @Override
    public boolean aggregatesNetworks() {
        return true;
    }

    public static class NewNetworkLabelModel extends NetworkLabelModel {

        public NewNetworkLabelModel(HostSetupNetworksModel setupModel) {
            super(ConstantsManager.getInstance().getConstants().newLabelPanelText(), setupModel);
        }
    }

}
