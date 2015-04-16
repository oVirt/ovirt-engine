package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmId", getVmId())
                .append("status", getStatus())
                .append("exitStatus", getExitStatus());
    }
}
