package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainParametersBase extends StoragePoolParametersBase {
    private static final long serialVersionUID = -3426166529161499883L;

    public enum Phase {
        PROCESS_OVF_UPDATE_FOR_STORAGE_POOL,
        PROCESS_OVF_UPDATE_FOR_STORAGE_DOMAIN
    }

    private Guid storageDomainId;
    private boolean isInternal;
    private Guid quotaId;
    private Guid diskProfileId;
    private Phase phase = Phase.PROCESS_OVF_UPDATE_FOR_STORAGE_POOL;

    public StorageDomainParametersBase() {
        storageDomainId = Guid.Empty;
    }

    public StorageDomainParametersBase(Guid storageDomainId) {
        super(Guid.Empty);
        setStorageDomainId(storageDomainId);
    }

    public StorageDomainParametersBase(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId);
        setStorageDomainId(storageDomainId);
    }

    public StorageDomainParametersBase(StorageDomainParametersBase other) {
        super(other);
        this.storageDomainId = other.storageDomainId;
        this.isInternal = other.isInternal;
        this.quotaId = other.quotaId;
        this.diskProfileId = other.diskProfileId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }

    public boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(boolean value) {
        isInternal = value;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public Guid getDiskProfileId() {
        return diskProfileId;
    }

    public void setDiskProfileId(Guid diskProfileId) {
        this.diskProfileId = diskProfileId;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }
}
