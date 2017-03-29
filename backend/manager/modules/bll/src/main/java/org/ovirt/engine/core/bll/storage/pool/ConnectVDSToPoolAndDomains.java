package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
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

public class ConnectVDSToPoolAndDomains extends ActivateDeactivateSingleAsyncOperation {

    private static final Logger log = LoggerFactory.getLogger(ConnectVDSToPoolAndDomains.class);

    private Guid masterStorageDomainId;

    private List<StoragePoolIsoMap> storagePoolIsoMap;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Inject
    private StorageHelperDirector storageHelperDirector;

    public ConnectVDSToPoolAndDomains(List<VDS> vdss, StorageDomain domain, StoragePool storagePool) {
        super(vdss, domain, storagePool);
    }

    @PostConstruct
    private void init() {
        masterStorageDomainId = storageDomainDao.getMasterStorageDomainIdForPool(getStoragePool().getId());
        storagePoolIsoMap = storagePoolIsoMapDao.getAllForStoragePool(getStoragePool().getId());
    }

    @Override
    public void execute(int iterationId) {
        VDS vds = getVdss().get(iterationId);
        try {
            boolean isConnectSucceeded =
                    storageHelperDirector.getItem(getStorageDomain().getStorageType())
                            .connectStorageToDomainByVdsId(getStorageDomain(), vds.getId());
            if (isConnectSucceeded) {
                resourceManager.runVdsCommand(
                        VDSCommandType.ConnectStoragePool,
                        new ConnectStoragePoolVDSCommandParameters(
                                vds, getStoragePool(), masterStorageDomainId, storagePoolIsoMap));
            } else {
                log.error("Failed to connect host '{}' to domain '{}'",
                        vds.getName(),
                        getStorageDomain().getStorageName());
            }
        } catch (RuntimeException e) {
            log.error("Failed to connect host '{}' to storage pool '{}': {}",
                    vds.getName(),
                    getStoragePool().getName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

}
