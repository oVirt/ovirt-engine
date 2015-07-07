package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmFromImportExportParameters extends RemoveVmParameters implements Serializable {
    private static final long serialVersionUID = 1841755064122049392L;

    private Guid storagePoolId;
    private Guid storageDomainId;

    public RemoveVmFromImportExportParameters() {
        storagePoolId = Guid.Empty;
        storageDomainId = Guid.Empty;
    }

    public RemoveVmFromImportExportParameters(Guid vmId, Guid storageDomainId, Guid storagePoolId) {
        super(vmId, false);
        setStorageDomainId(storageDomainId);
        setStoragePoolId(storagePoolId);
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }
}
