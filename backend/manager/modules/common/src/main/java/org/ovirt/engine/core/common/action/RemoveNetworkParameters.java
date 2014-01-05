package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class RemoveNetworkParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -7392121807419409051L;

    @NotNull
    private Guid id;

    private boolean removeFromNetworkProvider;

    public RemoveNetworkParameters() {
    }

    public RemoveNetworkParameters(Guid id) {
        this.id = id;
    }

    public RemoveNetworkParameters(Guid id, boolean removeFromNetworkProvider) {
        this.id = id;
        this.removeFromNetworkProvider = removeFromNetworkProvider;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public boolean isRemoveFromNetworkProvider() {
        return removeFromNetworkProvider;
    }

    public void setRemoveFromNetworkProvider(boolean removeFromNetworkProvider) {
        this.removeFromNetworkProvider = removeFromNetworkProvider;
    }
}
