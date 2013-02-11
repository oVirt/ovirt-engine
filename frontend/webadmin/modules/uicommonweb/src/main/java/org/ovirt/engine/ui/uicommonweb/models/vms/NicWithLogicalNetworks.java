package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class NicWithLogicalNetworks extends ListModel {

    private VmNetworkInterface networkInterface;

    public NicWithLogicalNetworks(VmNetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
    }

    public VmNetworkInterface getNetworkInterface() {
        return networkInterface;
    }

}
