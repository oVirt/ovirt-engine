package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class GetExistingStorageDomainListParameters extends IdQueryParameters {
    private static final long serialVersionUID = 7478078947370484916L;
    private StorageType privateStorageType;

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    private StorageDomainType privateStorageDomainType;

    public StorageDomainType getStorageDomainType() {
        return privateStorageDomainType;
    }

    private void setStorageDomainType(StorageDomainType value) {
        privateStorageDomainType = value;
    }

    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    private void setPath(String value) {
        privatePath = value;
    }

    private StorageFormatType storageFormatType;

    public StorageFormatType getStorageFormatType() {
        return storageFormatType;
    }

    private void setStorageFormatType(StorageFormatType storageFormatType) {
        this.storageFormatType = storageFormatType;
    }

    public GetExistingStorageDomainListParameters(Guid vdsId, StorageType storageType,
            StorageDomainType storageDomainType, String path) {
        this(vdsId, storageType, storageDomainType, path, null);
    }

    public GetExistingStorageDomainListParameters(Guid vdsId, StorageType storageType,
            StorageDomainType storageDomainType, String path, StorageFormatType storageFormatType) {
        super(vdsId);
        setStorageType(storageType);
        setStorageDomainType(storageDomainType);
        setPath(path);
        setStorageFormatType(storageFormatType);
    }

    public GetExistingStorageDomainListParameters() {
        this(null, StorageType.UNKNOWN, StorageDomainType.Master, null);
    }
}
