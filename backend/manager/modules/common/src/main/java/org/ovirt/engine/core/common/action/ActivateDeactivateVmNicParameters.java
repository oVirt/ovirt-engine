package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class ActivateDeactivateVmNicParameters extends VmOperationParameterBase {

    private VmNetworkInterface nic;
    private PlugAction action;

    public ActivateDeactivateVmNicParameters(VmNetworkInterface nic, PlugAction action) {
        super();
        this.nic = nic;
        this.action = action;
    }

    public VmNetworkInterface getNic() {
        return nic;
    }

    public void setNic(VmNetworkInterface nic) {
        this.nic = nic;
    }

    public PlugAction getAction() {
        return action;
    }

    public void setAction(PlugAction action) {
        this.action = action;
    }

}
