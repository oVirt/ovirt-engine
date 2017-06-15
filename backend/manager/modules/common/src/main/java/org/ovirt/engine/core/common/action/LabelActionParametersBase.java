package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class LabelActionParametersBase extends ActionParametersBase {
    private static final long serialVersionUID = -799396982675260518L;
    private Guid labelId;

    public LabelActionParametersBase(Guid labelId) {
        this.labelId = labelId;
    }

    public Guid getLabelId() {
        return labelId;
    }

    public LabelActionParametersBase() {
    }
}
