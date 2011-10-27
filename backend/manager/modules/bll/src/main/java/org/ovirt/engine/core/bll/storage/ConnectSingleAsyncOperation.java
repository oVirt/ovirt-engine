package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class ConnectSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {

    public ConnectSingleAsyncOperation(java.util.ArrayList<VDS> vdss, storage_domains domain, storage_pool storagePool) {
        super(vdss, domain, storagePool);
    }

    @Override
    public void Execute(int iterationId) {
        try {
            StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                    .ConnectStorageToDomainByVdsId(getStorageDomain(), getVdss().get(iterationId).getvds_id());
        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect host {0} to storage pool {1}. Exception: {3}", getVdss()
                    .get(iterationId).getvds_name(), getStoragePool().getname(), e);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(ConnectSingleAsyncOperation.class);
}
