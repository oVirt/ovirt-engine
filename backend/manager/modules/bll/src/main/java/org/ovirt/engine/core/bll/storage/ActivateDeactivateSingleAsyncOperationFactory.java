package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;
import org.ovirt.engine.core.utils.ISingleAsyncOperationFactory;

public abstract class ActivateDeactivateSingleAsyncOperationFactory implements ISingleAsyncOperationFactory {
    private ArrayList<VDS> _vdss;
    private storage_domains _storageDomain;
    private storage_pool _storagePool;

    protected ArrayList<VDS> getVdss() {
        return _vdss;
    }

    protected storage_domains getStorageDomain() {
        return _storageDomain;
    }

    protected storage_pool getStoragePool() {
        return _storagePool;
    }

    public void initialize(ArrayList parameters) {
        if (!(parameters.get(0) instanceof ArrayList)) {
            throw new InvalidOperationException();
        }
        ArrayList l = (ArrayList) parameters.get(0);
        if (!l.isEmpty() && !(l.get(0) instanceof VDS)) {
            throw new InvalidOperationException();
        }
        _vdss = (ArrayList<VDS>) parameters.get(0);
        if (parameters.get(1) != null && !(parameters.get(1) instanceof storage_domains)) {
            throw new InvalidOperationException();
        }
        _storageDomain = (storage_domains) parameters.get(1);
        if (!(parameters.get(2) instanceof storage_pool)) {
            throw new InvalidOperationException();
        }
        _storagePool = (storage_pool) parameters.get(2);
    }

    public abstract ISingleAsyncOperation createSingleAsyncOperation();
}
