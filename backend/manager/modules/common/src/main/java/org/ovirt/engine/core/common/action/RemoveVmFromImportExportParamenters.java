package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmFromImportExportParamenters extends RemoveVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = 1841755064122049392L;

    private Guid storagePoolId = Guid.Empty;
    private Guid storageDomainId = Guid.Empty;

    public RemoveVmFromImportExportParamenters() {
    }

    public RemoveVmFromImportExportParamenters(Guid vmId, Guid storageDomainId, Guid storagePoolId) {
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
