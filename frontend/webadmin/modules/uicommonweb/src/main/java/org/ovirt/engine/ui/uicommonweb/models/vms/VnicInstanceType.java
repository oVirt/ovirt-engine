package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class VnicInstanceType extends ListModel<VnicProfileView> {

    private VmNetworkInterface networkInterface;

    public VnicInstanceType(VmNetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
    }

    public VmNetworkInterface getNetworkInterface() {
        return networkInterface;
    }

}
