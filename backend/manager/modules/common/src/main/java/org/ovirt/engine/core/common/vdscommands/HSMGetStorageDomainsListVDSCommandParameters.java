package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HSMGetStorageDomainsListVDSCommandParameters")
public class HSMGetStorageDomainsListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public HSMGetStorageDomainsListVDSCommandParameters(Guid vdsId, Guid storagePoolId, StorageType storageType,
            StorageDomainType storageDomainType, String path) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
        setStorageType(storageType);
        setStorageDomainType(storageDomainType);
        setPath(path);
    }

    @XmlElement
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    @XmlElement
    private StorageType privateStorageType = StorageType.forValue(0);

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    @XmlElement
    private StorageDomainType privateStorageDomainType = StorageDomainType.forValue(0);

    public StorageDomainType getStorageDomainType() {
        return privateStorageDomainType;
    }

    private void setStorageDomainType(StorageDomainType value) {
        privateStorageDomainType = value;
    }

    @XmlElement
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    private void setPath(String value) {
        privatePath = value;
    }

    public HSMGetStorageDomainsListVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storagePoolId=%s, storageType=%s, storageDomainType=%s, path=%s",
                super.toString(),
                getStoragePoolId(),
                getStorageType(),
                getStorageDomainType(),
                getPath());
    }
}
