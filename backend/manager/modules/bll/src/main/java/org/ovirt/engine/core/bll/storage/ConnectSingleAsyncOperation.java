package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ConnectSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {

    public ConnectSingleAsyncOperation(java.util.ArrayList<VDS> vdss, storage_domains domain, storage_pool storagePool) {
        super(vdss, domain, storagePool);
    }

    @Override
    public void execute(int iterationId) {
        try {
            StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                    .connectStorageToDomainByVdsId(getStorageDomain(), getVdss().get(iterationId).getId());
        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect host {0} to storage pool {1}. Exception: {3}", getVdss()
                    .get(iterationId).getvds_name(), getStoragePool().getname(), e);
        }
    }

    private static Log log = LogFactory.getLog(ConnectSingleAsyncOperation.class);
}
