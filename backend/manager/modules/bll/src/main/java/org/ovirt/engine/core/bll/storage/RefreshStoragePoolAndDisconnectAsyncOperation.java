package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class RefreshStoragePoolAndDisconnectAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    public RefreshStoragePoolAndDisconnectAsyncOperation(java.util.ArrayList<VDS> vdss, storage_domains domain,
            storage_pool storagePool) {
        super(vdss, domain, storagePool);
    }

    @Override
    public void Execute(int iterationId) {
        try {
            Guid masterDomainIdFromDb =
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .getMasterStorageDomainIdForPool(getStoragePool().getId());
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.RefreshStoragePool,
                            new RefreshStoragePoolVDSCommandParameters(getVdss().get(iterationId).getvds_id(),
                                    getStoragePool().getId(), masterDomainIdFromDb, getStoragePool()
                                            .getmaster_domain_version()));
            StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                    .DisconnectStorageFromDomainByVdsId(getStorageDomain(), getVdss().get(iterationId).getvds_id());
        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect/refresh storagePool. Host {0} to storage pool {1}. Exception: {3}",
                    getVdss().get(iterationId).getvds_name(), getStoragePool().getname(), e);
        }

    }

    private static LogCompat log = LogFactoryCompat.getLog(RefreshStoragePoolAndDisconnectAsyncOperation.class);
}
