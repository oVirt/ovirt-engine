package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class UnlabelNetworkParameters extends ActionParametersBase {

    private static final long serialVersionUID = 550272159431854471L;

    @NotNull
    private Guid networkId;

    public UnlabelNetworkParameters() {
    }

    public UnlabelNetworkParameters(Guid networkId) {
        this.networkId = networkId;
    }

    public Guid getNetworkId() {
        return networkId;
    }
}
