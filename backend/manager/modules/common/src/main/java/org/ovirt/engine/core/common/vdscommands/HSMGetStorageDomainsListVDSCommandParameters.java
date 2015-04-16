package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HSMGetStorageDomainsListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public HSMGetStorageDomainsListVDSCommandParameters(Guid vdsId, Guid storagePoolId, StorageType storageType,
            StorageDomainType storageDomainType, String path) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
        setStorageType(storageType);
        setStorageDomainType(storageDomainType);
        setPath(path);
    }

    private Guid privateStoragePoolId;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

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

    public HSMGetStorageDomainsListVDSCommandParameters() {
        privateStoragePoolId = Guid.Empty;
        privateStorageType = StorageType.UNKNOWN;
        privateStorageDomainType = StorageDomainType.Master;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storagePoolId", getStoragePoolId())
                .append("storageType", getStorageType())
                .append("storageDomainType", getStorageDomainType())
                .append("path", getPath());
    }
}
