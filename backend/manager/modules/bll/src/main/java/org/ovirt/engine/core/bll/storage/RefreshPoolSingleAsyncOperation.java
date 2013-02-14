package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RefreshStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class RefreshPoolSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private final java.util.ArrayList<Guid> _vdsIdsToSetNonOperational;

    public RefreshPoolSingleAsyncOperation(java.util.ArrayList<VDS> vdss, StorageDomain domain,
            storage_pool storagePool, ArrayList<Guid> vdssIdsToSetNonoperational) {
        super(vdss, domain, storagePool);
        _vdsIdsToSetNonOperational = vdssIdsToSetNonoperational;
    }

    @Override
    public void execute(int iterationId) {
        try {
            Guid masterDomainIdFromDb =
                    DbFacade.getInstance()
                            .getStorageDomainDao()
                            .getMasterStorageDomainIdForPool(getStoragePool().getId());

            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
                try {
                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.ConnectStoragePool,
                                    new ConnectStoragePoolVDSCommandParameters(getVdss().get(iterationId).getId(),
                                            getStoragePool().getId(), getVdss().get(iterationId).getVdsSpmId(),
                                            masterDomainIdFromDb, getStoragePool().getmaster_domain_version()));
                } catch (java.lang.Exception e) {
                    log.errorFormat("Could not connect vds {0} to pool {1} - moving host to non-operational", getVdss()
                            .get(iterationId).getVdsName(), getStoragePool().getname());
                    synchronized (_vdsIdsToSetNonOperational) {
                        _vdsIdsToSetNonOperational.add(getVdss().get(iterationId).getId());
                    }
                }
            } else {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.RefreshStoragePool,
                                new RefreshStoragePoolVDSCommandParameters(getVdss().get(iterationId).getId(),
                                        getStoragePool().getId(), masterDomainIdFromDb, getStoragePool()
                                                .getmaster_domain_version()));
                log.infoFormat("Refreshed vds {0} in pool {1}", getVdss().get(iterationId).getVdsName(),
                        getStoragePool().getname());
            }

        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect/refresh storagePool. Host {0} to storage pool {1}. Exception: {3}",
                    getVdss().get(iterationId).getVdsName(), getStoragePool().getname(), e);
        }
    }

    private static Log log = LogFactory.getLog(RefreshPoolSingleAsyncOperation.class);
}
