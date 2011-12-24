package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class VdsIdParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -232268659060166995L;

    private Guid vdsId;

    public VdsIdParametersBase() {
    }

    public VdsIdParametersBase(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

}
