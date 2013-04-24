package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.utils.*;

public abstract class ActivateDeactivateSingleAsyncOperation implements ISingleAsyncOperation {
    private StorageDomain _domain;
    private StoragePool _pool;
    private java.util.ArrayList<VDS> _vdss;

    public ActivateDeactivateSingleAsyncOperation(java.util.ArrayList<VDS> vdss, StorageDomain domain,
            StoragePool storagePool) {
        _vdss = vdss;
        _domain = domain;
        _pool = storagePool;
    }

    public abstract void execute(int iterationId);

    protected StorageDomain getStorageDomain() {
        return _domain;
    }

    protected StoragePool getStoragePool() {
        return _pool;
    }

    protected java.util.ArrayList<VDS> getVdss() {
        return _vdss;
    }
}
