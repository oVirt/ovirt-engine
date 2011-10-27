package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.utils.*;

public abstract class ActivateDeactivateSingleAsyncOperation implements ISingleAsyncOperation {
    private storage_domains _domain;
    private storage_pool _pool;
    private java.util.ArrayList<VDS> _vdss;

    public ActivateDeactivateSingleAsyncOperation(java.util.ArrayList<VDS> vdss, storage_domains domain,
            storage_pool storagePool) {
        _vdss = vdss;
        _domain = domain;
        _pool = storagePool;
    }

    public abstract void Execute(int iterationId);

    protected storage_domains getStorageDomain() {
        return _domain;
    }

    protected storage_pool getStoragePool() {
        return _pool;
    }

    protected java.util.ArrayList<VDS> getVdss() {
        return _vdss;
    }
}
