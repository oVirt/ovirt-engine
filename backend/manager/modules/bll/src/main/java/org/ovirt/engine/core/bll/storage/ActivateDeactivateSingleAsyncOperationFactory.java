package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;
import org.ovirt.engine.core.utils.ISingleAsyncOperationFactory;

public abstract class ActivateDeactivateSingleAsyncOperationFactory implements ISingleAsyncOperationFactory {
    private ArrayList<VDS> _vdss;
    private StorageDomain _storageDomain;
    private StoragePool _storagePool;

    protected ArrayList<VDS> getVdss() {
        return _vdss;
    }

    protected StorageDomain getStorageDomain() {
        return _storageDomain;
    }

    protected StoragePool getStoragePool() {
        return _storagePool;
    }

    @Override
    public void initialize(ArrayList<?> parameters) {
        if (!(parameters.get(0) instanceof ArrayList)) {
            throw new IllegalArgumentException();
        }
        ArrayList l = (ArrayList) parameters.get(0);
        if (!l.isEmpty() && !(l.get(0) instanceof VDS)) {
            throw new IllegalArgumentException();
        }
        _vdss = (ArrayList<VDS>) parameters.get(0);
        if (parameters.get(1) != null && !(parameters.get(1) instanceof StorageDomain)) {
            throw new IllegalArgumentException();
        }
        _storageDomain = (StorageDomain) parameters.get(1);
        if (!(parameters.get(2) instanceof StoragePool)) {
            throw new IllegalArgumentException();
        }
        _storagePool = (StoragePool) parameters.get(2);
    }

    @Override
    public abstract ISingleAsyncOperation createSingleAsyncOperation();
}
