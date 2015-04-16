package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DetachStorageDomainVDSCommandParameters extends DeactivateStorageDomainVDSCommandParameters {
    public DetachStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            Guid masterStorageDomainId, int masterVersion) {
        super(storagePoolId, storageDomainId, masterStorageDomainId, masterVersion);
    }

    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

    private boolean detachFromOldStoragePool;

    public DetachStorageDomainVDSCommandParameters() {
    }

    public boolean isDetachFromOldStoragePool() {
        return detachFromOldStoragePool;
    }

    public void setDetachFromOldStoragePool(boolean detachFromOldStoragePool) {
        this.detachFromOldStoragePool = detachFromOldStoragePool;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("force", getForce());
    }
}
