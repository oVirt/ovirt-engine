package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshPoolSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private static final Logger log = LoggerFactory.getLogger(RefreshPoolSingleAsyncOperation.class);

    private final List<Guid> vdsIdsToSetNonOperational;

    private Guid masterStorageDomainId;

    private List<StoragePoolIsoMap> storagePoolIsoMap;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    public RefreshPoolSingleAsyncOperation(List<VDS> vdss, StorageDomain domain,
            StoragePool storagePool, List<Guid> vdssIdsToSetNonoperational) {
        super(vdss, domain, storagePool);
        vdsIdsToSetNonOperational = vdssIdsToSetNonoperational;
    }

    @PostConstruct
    private void init() {
        masterStorageDomainId = storageDomainDao.getMasterStorageDomainIdForPool(getStoragePool().getId());
        storagePoolIsoMap = storagePoolIsoMapDao.getAllForStoragePool(getStoragePool().getId());
    }

    @Override
    public void execute(int iterationId) {
        try {
            if (getStorageDomain() != null &&
                        getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
                try {
                    resourceManager
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
                resourceManager.runVdsCommand(
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
