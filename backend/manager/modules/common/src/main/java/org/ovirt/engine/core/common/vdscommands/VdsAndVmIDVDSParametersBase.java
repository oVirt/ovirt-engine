package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VdsAndVmIDVDSParametersBase extends VdsIdVDSCommandParametersBase {
    private Guid _vmId;

    public VdsAndVmIDVDSParametersBase(Guid vdsId, Guid vmId) {
        super(vdsId);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VdsAndVmIDVDSParametersBase() {
        _vmId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmId", getVmId());
    }
}
