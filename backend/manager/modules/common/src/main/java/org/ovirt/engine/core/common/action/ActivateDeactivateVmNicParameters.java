package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.network.VmNic;

public class ActivateDeactivateVmNicParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 8972183222927384462L;
    private VmNic nic;
    private PlugAction action;

    public ActivateDeactivateVmNicParameters(VmNic nic, PlugAction action) {
        super();
        this.nic = nic;
        this.action = action;
    }

    public VmNic getNic() {
        return nic;
    }

    public void setNic(VmNic nic) {
        this.nic = nic;
    }

    public PlugAction getAction() {
        return action;
    }

    public void setAction(PlugAction action) {
        this.action = action;
    }

}
