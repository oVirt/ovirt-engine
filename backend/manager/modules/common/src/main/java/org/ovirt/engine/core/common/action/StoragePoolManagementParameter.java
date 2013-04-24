package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.StoragePool;

public class StoragePoolManagementParameter extends StoragePoolParametersBase {
    private static final long serialVersionUID = -7879188389154192375L;
    @Valid
    private StoragePool privateStoragePool;

    public StoragePool getStoragePool() {
        return privateStoragePool;
    }

    private void setStoragePool(StoragePool value) {
        privateStoragePool = value;
    }

    public StoragePoolManagementParameter(StoragePool storagePool) {
        super(storagePool.getId());
        setStoragePool(storagePool);
    }

    public StoragePoolManagementParameter() {
    }
}
