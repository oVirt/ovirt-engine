package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public abstract class ActivateDeactivateSingleAsyncOperation implements ISingleAsyncOperation {
    private StorageDomain domain;
    private StoragePool pool;
    private List<VDS> vdss;

    public ActivateDeactivateSingleAsyncOperation(List<VDS> vdss, StorageDomain domain,
            StoragePool storagePool) {
        this.vdss = vdss;
        this.domain = domain;
        pool = storagePool;
    }

    @Override
    public abstract void execute(int iterationId);

    protected StorageDomain getStorageDomain() {
        return domain;
    }

    protected StoragePool getStoragePool() {
        return pool;
    }

    protected List<VDS> getVdss() {
        return vdss;
    }
}
