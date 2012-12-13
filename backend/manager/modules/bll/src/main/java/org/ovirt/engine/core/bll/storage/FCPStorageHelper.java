package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;

public class FCPStorageHelper extends StorageHelperBase {
    @Override
    public boolean connectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return true;
    }

    @Override
    public boolean disconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return true;
    }

    @Override
    protected boolean runConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type) {
        return true;
    }

    @Override
    public boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return true;
    }

    @Override
    public boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return true;
    }

    @Override
    public boolean StorageDomainRemoved(StorageDomainStatic storageDomain) {
        removeStorageDomainLuns(storageDomain);
        return true;
    }
}
