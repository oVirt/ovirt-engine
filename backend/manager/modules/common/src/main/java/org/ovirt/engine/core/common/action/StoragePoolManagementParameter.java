package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.storage_pool;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StoragePoolManagementParameter")
public class StoragePoolManagementParameter extends StoragePoolParametersBase {
    private static final long serialVersionUID = -7879188389154192375L;
    @Valid
    @XmlElement(name = "StoragePool")
    private storage_pool privateStoragePool;

    public storage_pool getStoragePool() {
        return privateStoragePool;
    }

    private void setStoragePool(storage_pool value) {
        privateStoragePool = value;
    }

    public StoragePoolManagementParameter(storage_pool storagePool) {
        super(storagePool.getId());
        setStoragePool(storagePool);
    }

    public StoragePoolManagementParameter() {
    }
}
