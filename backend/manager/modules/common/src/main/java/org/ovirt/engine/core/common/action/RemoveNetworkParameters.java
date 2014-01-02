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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + (isRemoveFromNetworkProvider() ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RemoveNetworkParameters other = (RemoveNetworkParameters) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        if (isRemoveFromNetworkProvider() != other.isRemoveFromNetworkProvider())
            return false;
        return true;
    }
}
