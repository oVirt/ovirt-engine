package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class DeleteImageGroupVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    private boolean postZeros;
    private boolean forceDelete;

    public DeleteImageGroupVDSCommandParameters(Guid storagePoolId,
            Guid storageDomainId, Guid imageGroupId, boolean postZeros, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId);
        setPostZeros(postZeros);
        setForceDelete(force);
    }

    public DeleteImageGroupVDSCommandParameters() { }

    public boolean getPostZeros() {
        return postZeros;
    }

    protected void setPostZeros(boolean value) {
        postZeros = value;
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
                getPostZeros(),
                getForceDelete());
    }
}
