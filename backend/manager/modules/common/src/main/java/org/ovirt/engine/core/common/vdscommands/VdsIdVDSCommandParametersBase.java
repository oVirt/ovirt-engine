package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VdsIdVDSCommandParametersBase extends VDSParametersBase {
    public VdsIdVDSCommandParametersBase(Guid vdsId) {
        _vdsId = vdsId;
    }

    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    public void setVdsId(Guid _vdsId) {
        this._vdsId = _vdsId;
    }

    public VdsIdVDSCommandParametersBase() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("hostId", getVdsId());
    }
}
