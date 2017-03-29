package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AfterDeactivateSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private static final Logger log = LoggerFactory.getLogger(AfterDeactivateSingleAsyncOperation.class);

    private final boolean isLastMaster;
    private Guid masterStorageDomainId = Guid.Empty;
    private List<StoragePoolIsoMap> storagePoolIsoMap;

    @Inject
    private VDSBrokerFrontend resourceManager;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Inject
    private StorageHelperDirector storageHelperDirector;

    public AfterDeactivateSingleAsyncOperation(List<VDS> vdss, StorageDomain domain,
            StoragePool storagePool, boolean isLastMaster, Guid newMasterStorageDomain) {
        super(vdss, domain, storagePool);

        this.isLastMaster = isLastMaster;
        this.masterStorageDomainId = newMasterStorageDomain;
    }

    @PostConstruct
    private void init() {
        if (masterStorageDomainId == null || masterStorageDomainId.equals(Guid.Empty)) {
            this.masterStorageDomainId = storageDomainDao.getMasterStorageDomainIdForPool(getStoragePool().getId());
        }

        storagePoolIsoMap = storagePoolIsoMapDao.getAllForStoragePool(getStoragePool().getId());
    }

    @Override
    public void execute(int iterationId) {
        try {
            log.info("After deactivate treatment vds '{}', pool '{}'", getVdss().get(iterationId).getName(),
                    getStoragePool().getName());

            if (!isLastMaster) {
                resourceManager.runVdsCommand(
                        VDSCommandType.ConnectStoragePool, new ConnectStoragePoolVDSCommandParameters(
                        getVdss().get(iterationId), getStoragePool(), masterStorageDomainId, storagePoolIsoMap, true));
            }

            if (getVdss().get(iterationId).getSpmStatus() == VdsSpmStatus.None) {
                storageHelperDirector.getItem(getStorageDomain().getStorageType())
                        .disconnectStorageFromDomainByVdsId(getStorageDomain(), getVdss().get(iterationId).getId());
            }
        } catch (RuntimeException e) {
            log.error("Failed to refresh storagePool. Host '{}' to storage pool '{}': {}",
                    getVdss().get(iterationId).getName(),
                    getStoragePool().getName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }
}
