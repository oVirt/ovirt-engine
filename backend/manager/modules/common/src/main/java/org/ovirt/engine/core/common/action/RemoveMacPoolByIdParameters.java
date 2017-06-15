package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class RemoveMacPoolByIdParameters extends ActionParametersBase {

    @NotNull
    private Guid macPoolId;

    public RemoveMacPoolByIdParameters() {
    }

    public RemoveMacPoolByIdParameters(Guid macPoolId) {
        this.macPoolId = macPoolId;
    }

    public Guid getMacPoolId() {
        return macPoolId;
    }
}

