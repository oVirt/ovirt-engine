package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class UpdateVmVersionParameters extends VmManagementParametersBase {

    private Guid vmPoolId;

    public UpdateVmVersionParameters() {
    }

    public UpdateVmVersionParameters(Guid vmId) {
        super();
        setVmId(vmId);
    }

    public Guid getVmPoolId() {
        return vmPoolId;
    }

    public void setVmPoolId(Guid vmPoolId) {
        this.vmPoolId = vmPoolId;
    }
}
