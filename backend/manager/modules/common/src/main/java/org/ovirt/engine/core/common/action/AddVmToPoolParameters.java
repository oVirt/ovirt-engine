package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class AddVmToPoolParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = 1019066360476623259L;
    private Guid _vmId;

    public AddVmToPoolParameters(Guid vmPoolId, Guid vmId) {
        super(vmPoolId);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public AddVmToPoolParameters() {
        _vmId = Guid.Empty;
    }
}
