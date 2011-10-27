package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class AfterDeactivateSingleAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private boolean _isLastMaster;
    private Guid _newMasterStorageDomainId = new Guid();

    public AfterDeactivateSingleAsyncOperation(java.util.ArrayList<VDS> vdss, storage_domains domain,
            storage_pool storagePool, boolean isLastMaster, Guid newMasterStorageDomain) {
        super(vdss, domain, storagePool);
        _isLastMaster = isLastMaster;
        _newMasterStorageDomainId = newMasterStorageDomain;
    }

    @Override
    public void Execute(int iterationId) {
        try {
            log.infoFormat("After deactivate treatment vds: {0},pool {1}", getVdss().get(iterationId).getvds_name(),
                    getStoragePool().getname());
            if (!_isLastMaster) {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.RefreshStoragePool,
                                new RefreshStoragePoolVDSCommandParameters(getVdss().get(iterationId).getvds_id(),
                                        getStoragePool().getId(),
                                        !_newMasterStorageDomainId.equals(Guid.Empty) ? _newMasterStorageDomainId
                                                : DbFacade.getInstance()
                                                        .getStorageDomainDAO()
                                                        .getMasterStorageDomainIdForPool(
                                                                getStoragePool().getId()), getStoragePool()
                                                .getmaster_domain_version()));
            }

            if (getVdss().get(iterationId).getspm_status() == VdsSpmStatus.None) {
                StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                        .DisconnectStorageFromDomainByVdsId(getStorageDomain(), getVdss().get(iterationId).getvds_id());
            }
        } catch (RuntimeException e) {
            log.errorFormat("Failed to refresh storagePool. Host {0} to storage pool {1}. Exception: {3}", getVdss()
                    .get(iterationId).getvds_name(), getStoragePool().getname(), e);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(AfterDeactivateSingleAsyncOperation.class);
}
