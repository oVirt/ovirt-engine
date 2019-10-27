package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public final class UnmanagedStorageDomainManagementParameter extends StorageDomainManagementParameter {

    public UnmanagedStorageDomainManagementParameter() {
    }

    public UnmanagedStorageDomainManagementParameter(StorageDomainStatic storageDomain) {
        super(storageDomain);
        getStorageDomain().setStorage(Guid.Empty.toString());
        getStorageDomain().setStorageFormat(StorageFormatType.V1);
        getStorageDomain().setDiscardAfterDelete(false);
        getStorageDomain().setBackup(false);
        getStorageDomain().setWipeAfterDelete(false);
        getStorageDomain().setStorageType(StorageType.UNMANAGED);
        getStorageDomain().setStorageDomainType(StorageDomainType.Unmanaged);
    }
}
