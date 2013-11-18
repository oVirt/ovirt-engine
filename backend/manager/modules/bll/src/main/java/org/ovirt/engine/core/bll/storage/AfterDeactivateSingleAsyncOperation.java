package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;


public class AfterDeactivateSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private final boolean _isLastMaster;
    private Guid masterStorageDomainId = Guid.Empty;
    private List<StoragePoolIsoMap> storagePoolIsoMap;

    public AfterDeactivateSingleAsyncOperation(ArrayList<VDS> vdss, StorageDomain domain,
            StoragePool storagePool, boolean isLastMaster, Guid newMasterStorageDomain) {
        super(vdss, domain, storagePool);

        _isLastMaster = isLastMaster;

        if (masterStorageDomainId != null && !masterStorageDomainId.equals(Guid.Empty)) {
            this.masterStorageDomainId = newMasterStorageDomain;
        } else {
            this.masterStorageDomainId = DbFacade.getInstance().getStorageDomainDao()
                    .getMasterStorageDomainIdForPool(getStoragePool().getId());
        }

        storagePoolIsoMap = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(getStoragePool().getId());
    }

    @Override
    public void execute(int iterationId) {
        try {
            log.infoFormat("After deactivate treatment vds: {0},pool {1}", getVdss().get(iterationId).getName(),
                    getStoragePool().getName());

            if (!_isLastMaster) {
                Backend.getInstance().getResourceManager().RunVdsCommand(
                        VDSCommandType.ConnectStoragePool, new ConnectStoragePoolVDSCommandParameters(
                        getVdss().get(iterationId), getStoragePool(), masterStorageDomainId, storagePoolIsoMap, true));
            }

            if (getVdss().get(iterationId).getSpmStatus() == VdsSpmStatus.None) {
                StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
                        .disconnectStorageFromDomainByVdsId(getStorageDomain(), getVdss().get(iterationId).getId());
            }
        } catch (RuntimeException e) {
            log.errorFormat("Failed to refresh storagePool. Host {0} to storage pool {1}. Exception: {3}", getVdss()
                    .get(iterationId).getName(), getStoragePool().getName(), e);
        }
    }

    private static final Log log = LogFactory.getLog(AfterDeactivateSingleAsyncOperation.class);
}
