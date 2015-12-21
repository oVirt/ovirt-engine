package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshPoolSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private static final Logger log = LoggerFactory.getLogger(RefreshPoolSingleAsyncOperation.class);

    private final ArrayList<Guid> vdsIdsToSetNonOperational;

    private Guid masterStorageDomainId;

    private List<StoragePoolIsoMap> storagePoolIsoMap;

    public RefreshPoolSingleAsyncOperation(ArrayList<VDS> vdss, StorageDomain domain,
            StoragePool storagePool, ArrayList<Guid> vdssIdsToSetNonoperational) {
        super(vdss, domain, storagePool);
        vdsIdsToSetNonOperational = vdssIdsToSetNonoperational;
        masterStorageDomainId = DbFacade.getInstance().getStorageDomainDao()
                .getMasterStorageDomainIdForPool(getStoragePool().getId());
        storagePoolIsoMap = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(getStoragePool().getId());
    }

    @Override
    public void execute(int iterationId) {
        try {
            if (getStorageDomain() != null &&
                        getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
                try {
                    Backend.getInstance()
                            .getResourceManager()
                            .runVdsCommand(
                                    VDSCommandType.ConnectStoragePool,
                                    new ConnectStoragePoolVDSCommandParameters(getVdss().get(iterationId),
                                            getStoragePool(), masterStorageDomainId, storagePoolIsoMap));
                } catch (Exception e) {
                    log.error("Could not connect vds '{}' to pool '{}' - moving host to non-operational: {}",
                            getVdss().get(iterationId).getName(),
                            getStoragePool().getName(),
                            e.getMessage());
                    log.debug("Exception", e);
                    synchronized (vdsIdsToSetNonOperational) {
                        vdsIdsToSetNonOperational.add(getVdss().get(iterationId).getId());
                    }
                }
            } else {
                Backend.getInstance().getResourceManager().runVdsCommand(
                        VDSCommandType.ConnectStoragePool,
                        new ConnectStoragePoolVDSCommandParameters(getVdss().get(iterationId), getStoragePool(),
                                masterStorageDomainId, storagePoolIsoMap, true));
                log.info("Refreshed vds '{}' in pool '{}'", getVdss().get(iterationId).getName(),
                        getStoragePool().getName());
            }

        } catch (RuntimeException e) {
            log.error("Failed to connect/refresh storagePool. Host '{}' to storage pool '{}': {}",
                    getVdss().get(iterationId).getName(),
                    getStoragePool().getName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }
}
