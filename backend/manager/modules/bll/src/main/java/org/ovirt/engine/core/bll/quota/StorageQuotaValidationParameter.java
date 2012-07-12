package org.ovirt.engine.core.bll.quota;

import org.ovirt.engine.core.compat.Guid;

public class StorageQuotaValidationParameter {
    Guid quotaId;
    Guid storageDomainId;
    double size;

    public StorageQuotaValidationParameter(Guid quotaId, Guid storageDomainId, double size) {
        this.quotaId = quotaId;
        this.storageDomainId = storageDomainId;
        this.size = size;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public double getSize() {
        return size;
    }

}
