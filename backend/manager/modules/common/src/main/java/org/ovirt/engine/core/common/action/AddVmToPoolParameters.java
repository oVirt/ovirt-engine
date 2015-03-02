package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class AddVmToPoolParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = 1019066360476623259L;
    private Guid vmId;

    public AddVmToPoolParameters() {
        vmId = Guid.Empty;
    }

    public AddVmToPoolParameters(Guid vmPoolId, Guid vmId) {
        super(vmPoolId);
        this.vmId = vmId;
    }

    public Guid getVmId() {
        return vmId;
    }
}
