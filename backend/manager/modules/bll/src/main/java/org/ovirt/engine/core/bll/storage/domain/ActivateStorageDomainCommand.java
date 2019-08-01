package org.ovirt.engine.core.bll.storage.domain;

import static org.ovirt.engine.core.common.businessentities.NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.storage.pool.RefreshPoolSingleAsyncOperationFactory;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ActivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ActivateStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {

    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private CINDERStorageHelper cinderStorageHelper;

    public ActivateStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public ActivateStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        StoragePoolValidator validator = createStoragePoolValidator();
        return validate(validator.exists())
                && validate(validator.isNotInStatus(StoragePoolStatus.Uninitialized))
                && checkStorageDomain()
                && storageDomainStatusIsValid()
                && (getStorageDomain().getStorageDomainType() == StorageDomainType.Master || checkMasterDomainIsUp())
                && checkForActiveVds() != null;
    }

    private void syncStorageDomainInfo(List<Pair<Guid, Boolean>> hostConnectionInfo) {
        for (Pair<Guid, Boolean> pair : hostConnectionInfo) {
            if (Boolean.TRUE.equals(pair.getSecond())) {
                if (storageHelperDirector.getItem(getStorageDomain().getStorageType())
                        .syncDomainInfo(getStorageDomain(), pair.getFirst())) {
                    break;
                }
            }
        }
    }

    @Override
    protected void executeCommand() {
        if (isManagedBlockStorageDomain()) {
            activateManageBlockStorageDomain();
            return;
        }

        if (isCinderStorageDomain()) {
            activateCinderStorageDomain();
            return;
        }
        final StoragePoolIsoMap map = storagePoolIsoMapDao
                        .get(new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
        // Master domain must not go through the Activating status.
        changeStorageDomainStatusInTransaction(map,
                (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) ?
                        StorageDomainStatus.Locked : StorageDomainStatus.Activating);
        freeLock();

        log.info("ActivateStorage Domain. Before Connect all hosts to pool. Time: {}", new Date());
        List<Pair<Guid, Boolean>> hostsConnectionResults = connectHostsInUpToDomainStorageServer();
        if (isAllHostConnectionFailed(hostsConnectionResults)) {
            log.error("Cannot connect storage server, aborting Storage Domain activation.");
            setSucceeded(false);
            return;
        }
        syncStorageDomainInfo(hostsConnectionResults);

        runVdsCommand(VDSCommandType.ActivateStorageDomain,
                new ActivateStorageDomainVDSCommandParameters(getStoragePool().getId(), getStorageDomain().getId()));
        log.info("ActivateStorage Domain. After Connect all hosts to pool. Time: {}", new Date());

        TransactionSupport.executeInNewTransaction(() -> {
            map.setStatus(StorageDomainStatus.Active);
            storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());
            if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
                calcStoragePoolStatusByDomainsStatus();
            }
            return null;
        });
        refreshAllVdssInPool();

        log.info("ActivateStorage Domain. After change storage pool status in vds. Time: {}",
                new Date());
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.ISO) {
            isoDomainListSynchronizer.refresheIsoDomainWhenActivateDomain(getStorageDomain().getId(),
                    getStoragePool().getId());
        }
        setSucceeded(true);
    }

    private boolean isAllHostConnectionFailed(List<Pair<Guid, Boolean>> hostsConnectionResults) {
        return hostsConnectionResults.stream().map(Pair::getSecond).noneMatch(Boolean.TRUE::equals);
    }

    private void activateManageBlockStorageDomain() {
        StoragePoolIsoMap map = storagePoolIsoMapDao.get(new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId()));
        map.setStatus(StorageDomainStatus.Active);
        storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());
        setSucceeded(true);
    }

    private void activateCinderStorageDomain() {
        List<Pair<Guid, Boolean>> hostsConnectionResults = connectHostsInUpToDomainStorageServer();
        for (Pair<Guid, Boolean> pair : hostsConnectionResults) {
            if (!pair.getSecond()) {
                log.error("Failed to activate Cinder storage domain '{}' due to secrets registration failure.",
                        getStorageDomain().getName());
                return;
            }
        }
        cinderStorageHelper.activateCinderDomain(getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
        setSucceeded(true);
    }

    private void refreshAllVdssInPool() {
        final List<Guid> vdsIdsToSetNonOperational = new ArrayList<>();

        getEventQueue().submitEventSync(
                new Event(getParameters().getStoragePoolId(), getParameters().getStorageDomainId(), null, EventType.POOLREFRESH, ""),
                () -> {
                    runSynchronizeOperation(new RefreshPoolSingleAsyncOperationFactory(), vdsIdsToSetNonOperational);
                    return null;
                }
        );

        for (Guid vdsId : vdsIdsToSetNonOperational) {
            Map<String, String> customLogValues = Collections.singletonMap("StorageDomainNames", getStorageDomainName());
            SetNonOperationalVdsParameters tempVar =
                    new SetNonOperationalVdsParameters(vdsId, STORAGE_DOMAIN_UNREACHABLE, customLogValues);
            tempVar.setStorageDomainId(getStorageDomain().getId());
            tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            runInternalAction(ActionType.SetNonOperationalVds,
                    tempVar,
                    ExecutionHandler.createInternalJobContext(getContext()));
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
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
        addValidationMessage(EngineMessage.VAR__ACTION__ACTIVATE);
    }

    private boolean storageDomainStatusIsValid() {
        boolean returnValue;
        if (isInternalExecution()) {
            returnValue = checkStorageDomainStatus(StorageDomainStatus.Inactive, StorageDomainStatus.Unknown,
                    StorageDomainStatus.Locked, StorageDomainStatus.Maintenance,
                    StorageDomainStatus.PreparingForMaintenance);
        } else {
            returnValue = checkStorageDomainStatus(StorageDomainStatus.Inactive, StorageDomainStatus.Unknown,
                    StorageDomainStatus.Maintenance, StorageDomainStatus.PreparingForMaintenance);
        }
        return returnValue;
    }
}
