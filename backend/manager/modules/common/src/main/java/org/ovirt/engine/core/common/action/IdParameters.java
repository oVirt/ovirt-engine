package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class IdParameters extends ActionParametersBase {
    private static final long serialVersionUID = -8078914032408357639L;

    private Guid id;

    public IdParameters() {
    }

    public IdParameters(Guid id) {
        this.id = id;
    }

    public Guid getId() {
        return id;
    }
}

