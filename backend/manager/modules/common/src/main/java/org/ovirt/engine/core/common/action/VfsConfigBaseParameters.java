package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public abstract class VfsConfigBaseParameters extends VdsActionParameters {

    private static final long serialVersionUID = 8442994960202140298L;

    @NotNull
    private Guid nicId;

    public VfsConfigBaseParameters() {
    }

    public VfsConfigBaseParameters(Guid nicId) {
        this.nicId = nicId;
    }

    public Guid getNicId() {
        return nicId;
    }

    public void setNicId(Guid nicId) {
        this.nicId = nicId;
    }
}
