package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public abstract class ActivateDeactivateSingleAsyncOperation implements ISingleAsyncOperation {
    private StorageDomain _domain;
    private StoragePool _pool;
    private ArrayList<VDS> _vdss;

    public ActivateDeactivateSingleAsyncOperation(ArrayList<VDS> vdss, StorageDomain domain,
            StoragePool storagePool) {
        _vdss = vdss;
        _domain = domain;
        _pool = storagePool;
    }

    @Override
    public abstract void execute(int iterationId);

    protected StorageDomain getStorageDomain() {
        return _domain;
    }

    protected StoragePool getStoragePool() {
        return _pool;
    }

    protected ArrayList<VDS> getVdss() {
        return _vdss;
    }
}
