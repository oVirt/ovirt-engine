package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class VdsGroupParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -9133528679053901135L;
    private Guid _vdsGroupId;

    public VdsGroupParametersBase(Guid vdsGroupId) {
        _vdsGroupId = vdsGroupId;
    }

    public Guid getVdsGroupId() {
        return _vdsGroupId;
    }

    public VdsGroupParametersBase() {
    }
}
