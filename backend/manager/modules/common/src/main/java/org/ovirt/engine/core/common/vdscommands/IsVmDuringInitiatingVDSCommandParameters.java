package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmId", getVmId());
    }
}
