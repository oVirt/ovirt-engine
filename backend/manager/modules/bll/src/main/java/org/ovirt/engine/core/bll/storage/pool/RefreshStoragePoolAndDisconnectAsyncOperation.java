package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshStoragePoolAndDisconnectAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private static final Logger log = LoggerFactory.getLogger(RefreshStoragePoolAndDisconnectAsyncOperation.class);

    private Guid masterStorageDomainId;

    private List<StoragePoolIsoMap> storagePoolIsoMap;

    public RefreshStoragePoolAndDisconnectAsyncOperation(ArrayList<VDS> vdss, StorageDomain domain,
            StoragePool storagePool) {
        super(vdss, domain, storagePool);
        masterStorageDomainId = DbFacade.getInstance().getStorageDomainDao()
                .getMasterStorageDomainIdForPool(getStoragePool().getId());
        storagePoolIsoMap = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(getStoragePool().getId());
    }

    @Override
    public void execute(int iterationId) {
        try {
            Backend.getInstance().getResourceManager().runVdsCommand(
                    VDSCommandType.ConnectStoragePool,
                    new ConnectStoragePoolVDSCommandParameters(getVdss().get(iterationId), getStoragePool(),
                            masterStorageDomainId, storagePoolIsoMap, true));
            StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
                    .disconnectStorageFromDomainByVdsId(getStorageDomain(), getVdss().get(iterationId).getId());
        } catch (RuntimeException e) {
            log.error("Failed to connect/refresh storagePool. Host '{}' to storage pool '{}': {}",
                    getVdss().get(iterationId).getName(),
                    getStoragePool().getName(),
                    e.getMessage());
            log.debug("Exception", e);
        }

    }
}
