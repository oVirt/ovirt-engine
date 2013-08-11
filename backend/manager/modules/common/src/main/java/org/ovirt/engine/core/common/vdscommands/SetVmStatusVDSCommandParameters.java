package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;

public class SetVmStatusVDSCommandParameters extends VDSParametersBase {
    private Guid _vmId;
    private VMStatus _status;

    public SetVmStatusVDSCommandParameters(Guid vmId, VMStatus status) {
        _vmId = vmId;
        _status = status;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VMStatus getStatus() {
        return _status;
    }

    public SetVmStatusVDSCommandParameters() {
        _vmId = Guid.Empty;
        _status = VMStatus.Down;
    }

    @Override
    public String toString() {
        return String.format("vmId = %s, status = %s", getVmId(), getStatus());
    }
}
