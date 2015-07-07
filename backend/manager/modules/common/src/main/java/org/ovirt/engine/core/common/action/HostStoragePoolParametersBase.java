package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;

public class HostStoragePoolParametersBase extends StoragePoolParametersBase {

    private static final long serialVersionUID = 3203697290159189298L;
    private VDS vds;
    private StoragePool storagePool;

    public HostStoragePoolParametersBase() {
    }

    public HostStoragePoolParametersBase(VDS vds) {
        super(vds.getStoragePoolId(), vds.getId());
        this.vds = vds;
    }

    public HostStoragePoolParametersBase(StoragePool storage_pool, VDS vds) {
        super(storage_pool.getId(), vds.getId());
        this.vds = vds;
        this.storagePool = storage_pool;
    }

    public VDS getVds() {
        return vds;
    }

    public void setVds(VDS vds) {
        this.vds = vds;
    }

    public StoragePool getStoragePool() {
        return storagePool;
    }

    public void setStoragePool(StoragePool storage_pool) {
        this.storagePool = storage_pool;
    }

}
