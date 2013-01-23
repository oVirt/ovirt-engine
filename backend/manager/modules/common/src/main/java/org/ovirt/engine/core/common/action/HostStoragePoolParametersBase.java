package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_pool;

public class HostStoragePoolParametersBase extends StoragePoolParametersBase {

    private static final long serialVersionUID = 3203697290159189298L;
    private VDS vds;
    private storage_pool storagePool;

    public HostStoragePoolParametersBase(storage_pool storage_pool, VDS vds) {
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

    public storage_pool getStoragePool() {
        return storagePool;
    }

    public void setStoragePool(storage_pool storage_pool) {
        this.storagePool = storage_pool;
    }

}
