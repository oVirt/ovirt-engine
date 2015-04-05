package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class VfsConfigNetworkParameters extends VfsConfigBaseParameters {

    private static final long serialVersionUID = 8349966455466948610L;

    @NotNull
    private Guid networkId;

    public VfsConfigNetworkParameters() {
    }

    public VfsConfigNetworkParameters(Guid nicId, Guid networkId) {
        super(nicId);
        this.networkId = networkId;
    }

    public Guid getNetworkId() {
        return networkId;
    }
}
