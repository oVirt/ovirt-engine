package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveIscsiBondParameters extends ActionParametersBase {

    private static final long serialVersionUID = 5157688843104010403L;

    private Guid iscsiBondId;

    public RemoveIscsiBondParameters() {
    }

    public RemoveIscsiBondParameters(Guid iscsiBondId) {
        this.iscsiBondId = iscsiBondId;
    }

    public Guid getIscsiBondId() {
        return iscsiBondId;
    }
}
