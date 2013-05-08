package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class VdsActionParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 3959465593772384532L;

    boolean runSilent;

    public VdsActionParameters(Guid vdsId) {
        _vdsId = vdsId;
    }

    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    public void setVdsId(Guid value) {
        _vdsId = value;
    }

    public VdsActionParameters() {
    }

    public boolean isRunSilent() {
        return runSilent;
    }

    public void setRunSilent(boolean runSilent) {
        this.runSilent = runSilent;
    }
}
