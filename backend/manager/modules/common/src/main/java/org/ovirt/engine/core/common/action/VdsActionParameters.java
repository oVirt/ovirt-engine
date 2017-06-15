package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class VdsActionParameters extends ActionParametersBase {
    private static final long serialVersionUID = 3959465593772384532L;
    private Guid hostId;
    private boolean runSilent;

    public VdsActionParameters() {
    }

    public VdsActionParameters(Guid hostId) {
        this.hostId = hostId;
    }

    public Guid getVdsId() {
        return hostId;
    }

    public void setVdsId(Guid hostId) {
        this.hostId = hostId;
    }

    public boolean isRunSilent() {
        return runSilent;
    }

    public void setRunSilent(boolean runSilent) {
        this.runSilent = runSilent;
    }
}
