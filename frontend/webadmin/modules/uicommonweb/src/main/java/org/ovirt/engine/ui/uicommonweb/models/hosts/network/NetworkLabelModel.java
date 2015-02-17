package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;

public class NetworkLabelModel extends NetworkItemModel<NetworkStatus> {

    private final String label;
    private final List<LogicalNetworkModel> labelNetworks;

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

}
