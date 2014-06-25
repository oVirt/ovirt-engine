package org.ovirt.engine.core.bll.storage;

import static org.ovirt.engine.core.common.businessentities.NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.IsoDomainListSyncronizer;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ActivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class ActivateStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {

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
        boolean returnValue = checkStoragePool()
                && checkStoragePoolStatusNotEqual(StoragePoolStatus.Uninitialized,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL)
                && checkStorageDomain()
                && storageDomainStatusIsValid()
                && (getStorageDomain().getStorageDomainType() == StorageDomainType.Master || checkMasterDomainIsUp())
                && checkForActiveVds() != null;
        return returnValue;
    }

    @Override
    protected void executeCommand() {
        final StoragePoolIsoMap map =
                DbFacade.getInstance()
                        .getStoragePoolIsoMapDao()
                        .get(new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
        changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Locked);
        freeLock();

        log.infoFormat("ActivateStorage Domain. Before Connect all hosts to pool. Time:{0}", new Date());
        connectAllHostsToPool();
        runVdsCommand(VDSCommandType.ActivateStorageDomain,
                new ActivateStorageDomainVDSCommandParameters(getStoragePool().getId(), getStorageDomain().getId()));
        log.infoFormat("ActivateStorage Domain. After Connect all hosts to pool. Time:{0}", new Date());

        refreshAllVdssInPool();
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                map.setStatus(StorageDomainStatus.Active);
                DbFacade.getInstance().getStoragePoolIsoMapDao().updateStatus(map.getId(), map.getStatus());
                if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
                    calcStoragePoolStatusByDomainsStatus();
                }
                return null;
            }
        });

        log.infoFormat("ActivateStorage Domain. After change storage pool status in vds. Time:{0}",
                new Date());
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.ISO) {
            IsoDomainListSyncronizer.getInstance().refresheIsoDomainWhenActivateDomain(getStorageDomain().getId(),
                    getStoragePool().getId());
        }
        setSucceeded(true);
    }

    private void refreshAllVdssInPool() {
        List<Guid> vdsIdsToSetNonOperational = new ArrayList<Guid>();
        runSynchronizeOperation(new RefreshPoolSingleAsyncOperationFactory(), vdsIdsToSetNonOperational);
        for (Guid vdsId : vdsIdsToSetNonOperational) {
            Map<String, String> customLogValues = Collections.singletonMap("StorageDomainNames", getStorageDomainName());
            SetNonOperationalVdsParameters tempVar =
                    new SetNonOperationalVdsParameters(vdsId, STORAGE_DOMAIN_UNREACHABLE, customLogValues);
            tempVar.setSaveToDb(true);
            tempVar.setStorageDomainId(getStorageDomain().getId());
            tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            getBackend().runInternalAction(VdcActionType.SetNonOperationalVds,
                    tempVar,
                    ExecutionHandler.createInternalJobContext());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().isRunSilent()) {
            return getSucceeded() ? AuditLogType.USER_ACTIVATED_STORAGE_DOMAIN_ASYNC
                    : AuditLogType.USER_ACTIVATE_STORAGE_DOMAIN_FAILED_ASYNC;
        }
        return getSucceeded() ? AuditLogType.USER_ACTIVATED_STORAGE_DOMAIN
                : AuditLogType.USER_ACTIVATE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ACTIVATE);
    }

    private boolean storageDomainStatusIsValid() {
        boolean returnValue;
        if (isInternalExecution()) {
            returnValue = checkStorageDomainStatus(StorageDomainStatus.InActive, StorageDomainStatus.Unknown,
                    StorageDomainStatus.Locked, StorageDomainStatus.Maintenance);
        } else {
            returnValue = checkStorageDomainStatus(StorageDomainStatus.InActive, StorageDomainStatus.Unknown,
                    StorageDomainStatus.Maintenance);
        }
        return returnValue;
    }
}
