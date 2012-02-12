package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.IsoDomainListSyncronizer;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.vdscommands.ActivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@LockIdNameAttribute(fieldName = "StorageDomainId")
@NonTransactiveCommandAttribute(forceCompensation=true)
public class ActivateStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {

    private static Log log = LogFactory.getLog(ActivateStorageDomainCommand.class);

    public ActivateStorageDomainCommand(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ActivateStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean canDoAction() {
        super.canDoAction();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ACTIVATE);
        boolean returnValue = CheckStoragePool()
                && CheckStoragePoolStatusNotEqual(StoragePoolStatus.Uninitialized,
                                                  VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL)
                && CheckStorageDomain()
                && storageDomainStatusIsValid()
                && (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master || CheckMasterDomainIsUp());
        return returnValue;
    }

    @Override
    protected void executeCommand() {

        final storage_pool_iso_map map = DbFacade.getInstance().getStoragePoolIsoMapDAO().get(new StoragePoolIsoMapId(getParameters().getStorageDomainId(),getParameters().getStoragePoolId()));
        changeStorageDomainStatusInTransaction(map,StorageDomainStatus.Locked);
        freeLock();

        log.infoFormat("ActivateStorage Domain. Before Connect all hosts to pool. Time:{0}", new java.util.Date());
        ConnectAllHostsToPool();
        log.infoFormat("ActivateStorage Domain. After Connect all hosts to pool. Time:{0}", new java.util.Date());
        setSucceeded(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.ActivateStorageDomain,
                        new ActivateStorageDomainVDSCommandParameters(getStoragePool().getId(), getStorageDomain()
                                .getid())).getSucceeded());
        log.infoFormat("ActivateStorage Domain. After Activate storage domain in vds. Time:{0}", new java.util.Date());
        if (getSucceeded()) {
            RefreshAllVdssInPool(false);
            log.infoFormat("ActivateStorage Domain. After Refresh all pools . Time:{0}", new java.util.Date());

            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    getCompensationContext().snapshotEntityStatus(map, map.getstatus());
                    map.setstatus(StorageDomainStatus.Active);
                    DbFacade.getInstance().getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getstatus());
                    if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
                        CalcStoragePoolStatusByDomainsStatus();
                    }
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            log.infoFormat("ActivateStorage Domain. After change storage pool status in vds. Time:{0}",
                    new java.util.Date());
            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.ISO) {
                IsoDomainListSyncronizer.getInstance().refresheIsoDomainWhenActivateDomain(getStorageDomain().getid(),
                        getStoragePool().getId());
            }
        } else {
            compensate();
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ACTIVATED_STORAGE_DOMAIN
                : AuditLogType.USER_ACTIVATE_STORAGE_DOMAIN_FAILED;
    }

    private boolean storageDomainStatusIsValid() {
        boolean returnValue;
        if(isInternalExecution()) {
            returnValue = checkStorageDomainStatus(StorageDomainStatus.InActive, StorageDomainStatus.Unknown,
                    StorageDomainStatus.Locked, StorageDomainStatus.Maintenance);
        } else {
            returnValue = checkStorageDomainStatus(StorageDomainStatus.InActive, StorageDomainStatus.Unknown,
                    StorageDomainStatus.Maintenance);
        }
        return returnValue;
    }
}
