package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class VdsIdVDSCommandParametersBase extends VDSParametersBase {
    public VdsIdVDSCommandParametersBase(Guid vdsId) {
        _vdsId = vdsId;
    }

    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    public VdsIdVDSCommandParametersBase() {
    }

    @Override
    public String toString() {
        return String.format("HostId = %s", getVdsId());
    }
}
