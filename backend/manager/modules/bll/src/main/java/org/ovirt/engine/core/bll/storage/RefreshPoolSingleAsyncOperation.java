package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class RefreshPoolSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private final ArrayList<Guid> _vdsIdsToSetNonOperational;

    private Guid masterStorageDomainId;

    private List<StoragePoolIsoMap> storagePoolIsoMap;

    public RefreshPoolSingleAsyncOperation(ArrayList<VDS> vdss, StorageDomain domain,
            StoragePool storagePool, ArrayList<Guid> vdssIdsToSetNonoperational) {
        super(vdss, domain, storagePool);
        _vdsIdsToSetNonOperational = vdssIdsToSetNonoperational;
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
                            .RunVdsCommand(
                                    VDSCommandType.ConnectStoragePool,
                                    new ConnectStoragePoolVDSCommandParameters(getVdss().get(iterationId),
                                            getStoragePool(), masterStorageDomainId, storagePoolIsoMap));
                } catch (Exception e) {
                    log.errorFormat("Could not connect vds {0} to pool {1} - moving host to non-operational", getVdss()
                            .get(iterationId).getName(), getStoragePool().getName());
                    synchronized (_vdsIdsToSetNonOperational) {
                        _vdsIdsToSetNonOperational.add(getVdss().get(iterationId).getId());
                    }
                }
            } else {
                Backend.getInstance().getResourceManager().RunVdsCommand(
                        VDSCommandType.ConnectStoragePool,
                        new ConnectStoragePoolVDSCommandParameters(getVdss().get(iterationId), getStoragePool(),
                                masterStorageDomainId, storagePoolIsoMap, true));
                log.infoFormat("Refreshed vds {0} in pool {1}", getVdss().get(iterationId).getName(),
                        getStoragePool().getName());
            }

        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect/refresh storagePool. Host {0} to storage pool {1}. Exception: {3}",
                    getVdss().get(iterationId).getName(), getStoragePool().getName(), e);
        }
    }

    private static final Log log = LogFactory.getLog(RefreshPoolSingleAsyncOperation.class);
}
