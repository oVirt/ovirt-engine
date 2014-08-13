package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class DeleteImageGroupVDSCommandParameters
        extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters implements PostZero {
    private boolean postZero;
    private boolean forceDelete;

    public DeleteImageGroupVDSCommandParameters(Guid storagePoolId,
            Guid storageDomainId, Guid imageGroupId, boolean postZero, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId);
        setPostZero(postZero);
        setForceDelete(force);
    }

    public DeleteImageGroupVDSCommandParameters() { }

    @Override
    public boolean getPostZero() {
        return postZero;
    }

    @Override
    public void setPostZero(boolean postZero) {
        this.postZero = postZero;
    }

    public boolean getForceDelete() {
        return forceDelete;
    }

    public void setForceDelete(boolean value) {
        forceDelete = value;
    }

    @Override
    public String toString() {
        return String.format("%s, postZeros = %s, forceDelete = %s",
                super.toString(),
                getPostZero(),
                getForceDelete());
    }
}
