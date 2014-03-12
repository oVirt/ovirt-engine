package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ConnectSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {

    public ConnectSingleAsyncOperation(ArrayList<VDS> vdss, StorageDomain domain, StoragePool storagePool) {
        super(vdss, domain, storagePool);
    }

    @Override
    public void execute(int iterationId) {
        VDS vds = getVdss().get(iterationId);

        try {
            StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
                    .connectStorageToDomainByVdsId(getStorageDomain(), vds.getId());
        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect host {0} to storage domain (name: {1}, id: {2}). Exception: {3}",
                    vds.getName(), getStorageDomain().getName(), getStorageDomain().getId(), e);
        }
    }

    private static final Log log = LogFactory.getLog(ConnectSingleAsyncOperation.class);
}
