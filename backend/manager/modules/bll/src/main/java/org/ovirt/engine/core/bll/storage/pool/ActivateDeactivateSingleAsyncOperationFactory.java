package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;
import org.ovirt.engine.core.utils.ISingleAsyncOperationFactory;

public abstract class ActivateDeactivateSingleAsyncOperationFactory implements ISingleAsyncOperationFactory {
    private List<VDS> vdss;
    private StorageDomain storageDomain;
    private StoragePool storagePool;

    protected List<VDS> getVdss() {
        return vdss;
    }

    protected StorageDomain getStorageDomain() {
        return storageDomain;
    }

    protected StoragePool getStoragePool() {
        return storagePool;
    }

    @Override
    public void initialize(List<?> parameters) {
        if (!(parameters.get(0) instanceof List)) {
            throw new IllegalArgumentException();
        }
        List l = new ArrayList((List)parameters.get(0));
        if (!l.isEmpty() && !(l.get(0) instanceof VDS)) {
            throw new IllegalArgumentException();
        }
        vdss = (List<VDS>) l;
        if (parameters.get(1) != null && !(parameters.get(1) instanceof StorageDomain)) {
            throw new IllegalArgumentException();
        }
        storageDomain = (StorageDomain) parameters.get(1);
        if (!(parameters.get(2) instanceof StoragePool)) {
            throw new IllegalArgumentException();
        }
        storagePool = (StoragePool) parameters.get(2);
    }

    @Override
    public abstract ISingleAsyncOperation createSingleAsyncOperation();
}
