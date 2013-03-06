package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class DeleteImageGroupVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public DeleteImageGroupVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            boolean postZeros, boolean force, String competabilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId);
        setPostZeros(postZeros);
        setForceDelete(force);
        setCompatibilityVersion(competabilityVersion);
    }

    private boolean privatePostZeros;

    public boolean getPostZeros() {
        return privatePostZeros;
    }

    protected void setPostZeros(boolean value) {
        privatePostZeros = value;
    }

    private boolean privateForceDelete;

    public boolean getForceDelete() {
        return privateForceDelete;
    }

    public void setForceDelete(boolean value) {
        privateForceDelete = value;
    }

    public DeleteImageGroupVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, postZeros = %s, forceDelete = %s",
                super.toString(),
                getPostZeros(),
                getForceDelete());
    }
}
