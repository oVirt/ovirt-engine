package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.compat.Guid;

public class SetVmStatusVDSCommandParameters extends VDSParametersBase {
    private Guid vmId;
    private VMStatus status;
    private VmExitStatus exitStatus;

    public SetVmStatusVDSCommandParameters(Guid vmId, VMStatus status) {
        this(vmId, status, VmExitStatus.Normal);
    }

    public SetVmStatusVDSCommandParameters(Guid vmId, VMStatus status, VmExitStatus exitStatus) {
        this.vmId = vmId;
        this.status = status;
        this.exitStatus = exitStatus;
    }

    public SetVmStatusVDSCommandParameters() {
        vmId = Guid.Empty;
        status = VMStatus.Down;
        exitStatus = VmExitStatus.Normal;
    }

    public Guid getVmId() {
        return vmId;
    }

    public VMStatus getStatus() {
        return status;
    }

    public VmExitStatus getExitStatus() {
        return exitStatus;
    }

    @Override
    public String toString() {
        return String.format("vmId = %s, status = %s, exit status = %s",
                getVmId(), getStatus(), getExitStatus());
    }
}
