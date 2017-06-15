package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class EndExternalStepParameters extends ActionParametersBase {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Guid id;
    private boolean exitStatus;

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public boolean getStatus() {
        return exitStatus;
    }

    public void setStatus(boolean status) {
        this.exitStatus = status;
    }

    public EndExternalStepParameters() {
    }

    public EndExternalStepParameters(Guid id, boolean status) {
        super();
        this.id = id;
        this.exitStatus = status;
    }
}
