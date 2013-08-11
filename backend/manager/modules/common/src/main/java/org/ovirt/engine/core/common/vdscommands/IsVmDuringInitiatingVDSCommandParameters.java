package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class IsVmDuringInitiatingVDSCommandParameters extends VDSParametersBase {
    public IsVmDuringInitiatingVDSCommandParameters(Guid vmId) {
        _vmId = vmId;
    }

    private Guid _vmId;

    public Guid getVmId() {
        return _vmId;
    }

    public IsVmDuringInitiatingVDSCommandParameters() {
        _vmId = Guid.Empty;
    }

    @Override
    public String toString() {
        return String.format("vmId = %s", getVmId());
    }
}
