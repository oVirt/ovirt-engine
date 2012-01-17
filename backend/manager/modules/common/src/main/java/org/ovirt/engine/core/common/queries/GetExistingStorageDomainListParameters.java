package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetExistingStorageDomainListParameters")
public class GetExistingStorageDomainListParameters extends VdsIdParametersBase {
    private static final long serialVersionUID = 7478078947370484916L;
    @XmlElement(name = "StorageType")
    private StorageType privateStorageType = StorageType.forValue(0);

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    @XmlElement(name = "StorageDomainType")
    private StorageDomainType privateStorageDomainType = StorageDomainType.forValue(0);

    public StorageDomainType getStorageDomainType() {
        return privateStorageDomainType;
    }

    private void setStorageDomainType(StorageDomainType value) {
        privateStorageDomainType = value;
    }

    @XmlElement(name = "Path")
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    private void setPath(String value) {
        privatePath = value;
    }

    @XmlElement(name = "StorageFormatType")
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
    }
}
