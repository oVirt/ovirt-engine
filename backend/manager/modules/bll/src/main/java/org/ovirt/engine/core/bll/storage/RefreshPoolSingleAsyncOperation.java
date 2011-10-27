package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class RefreshPoolSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private java.util.ArrayList<Guid> _vdsIdsToSetNonOperational;

    public RefreshPoolSingleAsyncOperation(java.util.ArrayList<VDS> vdss, storage_domains domain,
            storage_pool storagePool, RefObject<java.util.ArrayList<Guid>> vdssIdsToSetNonoperational) {
        super(vdss, domain, storagePool);
        _vdsIdsToSetNonOperational = vdssIdsToSetNonoperational.argvalue;
    }

    @Override
    public void Execute(int iterationId) {
        try {
            Guid masterDomainIdFromDb =
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .getMasterStorageDomainIdForPool(getStoragePool().getId());

            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
                try {
                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.ConnectStoragePool,
                                    new ConnectStoragePoolVDSCommandParameters(getVdss().get(iterationId).getvds_id(),
                                            getStoragePool().getId(), getVdss().get(iterationId).getvds_spm_id(),
                                            masterDomainIdFromDb, getStoragePool().getmaster_domain_version()));
                } catch (java.lang.Exception e) {
                    log.errorFormat("Could not connect vds {0} to pool {1} - moving host to non-operational", getVdss()
                            .get(iterationId).getvds_name(), getStoragePool().getname());
                    synchronized (_vdsIdsToSetNonOperational) {
                        _vdsIdsToSetNonOperational.add(getVdss().get(iterationId).getvds_id());
                    }
                }
            } else {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.RefreshStoragePool,
                                new RefreshStoragePoolVDSCommandParameters(getVdss().get(iterationId).getvds_id(),
                                        getStoragePool().getId(), masterDomainIdFromDb, getStoragePool()
                                                .getmaster_domain_version()));
                log.infoFormat("Refreshed vds {0} in pool {1}", getVdss().get(iterationId).getvds_name(),
                        getStoragePool().getname());
            }

        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect/refresh storagePool. Host {0} to storage pool {1}. Exception: {3}",
                    getVdss().get(iterationId).getvds_name(), getStoragePool().getname(), e);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(RefreshPoolSingleAsyncOperation.class);
}
