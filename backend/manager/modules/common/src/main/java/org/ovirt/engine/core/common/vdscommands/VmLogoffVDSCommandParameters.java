package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmLogoffVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private boolean _force;

    public VmLogoffVDSCommandParameters(Guid vdsId, Guid vmId, boolean force) {
        super(vdsId, vmId);
        _force = force;
    }

    public boolean getForce() {
        return _force;
    }

    public VmLogoffVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("force", getForce());
    }
}
