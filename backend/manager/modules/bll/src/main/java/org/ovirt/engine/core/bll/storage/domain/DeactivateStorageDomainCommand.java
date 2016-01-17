package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.storage.pool.AfterDeactivateSingleAsyncOperationFactory;
import org.ovirt.engine.core.bll.storage.pool.DisconnectStoragePoolAsyncOperationFactory;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DeactivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.vdsbroker.irsbroker.SpmStopOnIrsVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class DeactivateStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {

    private boolean isLastMaster;

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        Scope scope = getParameters().isSkipLock() ? Scope.None : Scope.Execution;
        return lockProperties.withScope(scope);
    }

    public DeactivateStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    /**
     * Constructor for command creation when compensation is applied on startup
     */

    public DeactivateStorageDomainCommand(Guid commandId) {
        super(commandId);
    }


    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
        addValidationMessage(EngineMessage.VAR__ACTION__DEACTIVATE);
    }

    @Override
    protected boolean validate() {
        if (getParameters().isSkipChecks()) {
            return true;
        }

        if (!checkStorageDomain()) {
            return false;
        }

        // when the execution is internal, proceed also if the domain is in unknown status.
        if (!((getParameters().getIsInternal() && checkStorageDomainStatus(StorageDomainStatus.Active,
                StorageDomainStatus.Unknown)) || checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getParameters().getIsInternal()
                && getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
            List<StorageDomain> domains =
                    getStorageDomainDao().getAllForStoragePool(getStorageDomain().getStoragePoolId());

            List<StorageDomain> activeDomains = filterDomainsByStatus(domains, StorageDomainStatus.Active);

            List<StorageDomain> dataDomains = activeDomains.stream()
                    .filter(d -> d.getStorageDomainType() == StorageDomainType.Data).collect(Collectors.toList());

            if (!activeDomains.isEmpty() && dataDomains.isEmpty()) {
                return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_MASTER_WITH_NON_DATA_DOMAINS);
            }

            List<StorageDomain> busyDomains = domains.stream()
                    .filter(d -> d.getStatus().isStorageDomainInProcess()).collect(Collectors.toList());

            if (!busyDomains.isEmpty()) {
                return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_MASTER_WITH_LOCKED_DOMAINS);
            }
        }
        if (!isRunningVmsWithIsoAttached()) {
            return false;
        }
        if (!getParameters().getIsInternal()
                && !getVmDao()
                        .getAllActiveForStorageDomain(getStorageDomain().getId())
                        .isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DETECTED_ACTIVE_VMS);
        }
        if (getStoragePool().getSpmVdsId() != null) {
            // In case there are running tasks in the pool, it is impossible to deactivate the master storage domain
            if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master &&
                    getAsyncTaskDao().getAsyncTaskIdsByStoragePoolId(getStorageDomain().getStoragePoolId()).size() > 0) {
                return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_MASTER_DOMAIN_WITH_TASKS_ON_POOL);
            } else if (getStorageDomain().getStorageDomainType() != StorageDomainType.ISO &&
                    !getParameters().getIsInternal()
                    && (getAsyncTaskDao().getAsyncTaskIdsByEntity(getParameters().getStorageDomainId()).size() > 0 ||
                    getCommandEntityDao().getCommandIdsByEntity(getParameters().getStorageDomainId()).size() > 0)) {
                return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_TASKS);
            }
        }
        return true;
    }

    protected boolean isRunningVmsWithIsoAttached() {
        if (!getParameters().getIsInternal() && getStorageDomain().getStorageDomainType() == StorageDomainType.ISO) {
            List<String> vmNames = getVmsWithAttachedISO();
            if (!vmNames.isEmpty()) {
                return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED,
                        String.format("$VmNames %1$s", StringUtils.join(vmNames, ",")));
            }
        }
        return true;
    }

    protected List<String> getVmsWithAttachedISO() {
        List<VmStatic> vms = getVmStaticDao().getAllByStoragePoolId(getStorageDomain().getStoragePoolId());
        List<String> vmNames = new LinkedList<>();
        for (VmStatic vmStatic : vms) {
            VmDynamic vmDynamic = getVmDynamicDao().get(vmStatic.getId());
            if (vmDynamic.getStatus() != VMStatus.Down && !StringUtils.isEmpty(vmDynamic.getCurrentCd())) {
                vmNames.add(vmStatic.getName());
            }
        }
        return vmNames;
    }

    /**
     * Filter out the domains with the requested status from the given domains list, excluding the domain which the
     * command is run for.
     *
     * @param domains
     *            The domains to filter.
     * @param domainStatus
     *            The status to filter by.
     * @return Just the domains that match the given status, excluding the current domain of the command.
     */
    private List<StorageDomain> filterDomainsByStatus(List<StorageDomain> domains,
            final StorageDomainStatus domainStatus) {
        return domains.stream()
                .filter(d -> d.getStatus() == domainStatus && !d.getId().equals(getStorageDomain().getId()))
                .collect(Collectors.toList());
    }

    @Override
    protected void executeCommand() {
        if (isCinderStorageDomain()) {
            deactivateCinderStorageDomain();
            return;
        }
        final StoragePoolIsoMap map =
                getStoragePoolIsoMapDao().get
                        (new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
        map.setStatus(StorageDomainStatus.Unknown);
        changeStorageDomainStatusInTransaction(map,
                getParameters().isInactive() ? StorageDomainStatus.Locked : StorageDomainStatus.PreparingForMaintenance);

        final StorageDomain newMaster;

        if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
            newMaster = electNewMaster();
            isLastMaster = proceedStorageDomainTreatmentByDomainType(newMaster, true);
        } else {
            newMaster = null;
            isLastMaster = false;
        }

        final Guid newMasterId = newMaster != null ? newMaster.getId() : Guid.Empty;

        if (isLastMaster) {
            executeInNewTransaction(() -> {
                getCompensationContext().snapshotEntityStatus(getStoragePool());
                getStoragePool().setStatus(StoragePoolStatus.Maintenance);
                getStoragePoolDao().updateStatus(getStoragePool().getId(), getStoragePool().getStatus());
                getCompensationContext().stateChanged();
                return null;
            });

            StoragePoolStatusHandler.poolStatusChanged(getStoragePool().getId(), getStoragePool().getStatus());
            getStorageDomain().getStorageDynamicData().setAvailableDiskSize(null);
            getStorageDomain().getStorageDynamicData().setUsedDiskSize(null);
        }
        if (!getParameters().isInactive()) {
            runVdsCommand(VDSCommandType.DeactivateStorageDomain,
                    new DeactivateStorageDomainVDSCommandParameters(getStoragePool().getId(),
                            getStorageDomain().getId(),
                            newMasterId,
                            getStoragePool().getMasterDomainVersion()));
        }
        freeLock();

        VDS spm = null;
        if (getStoragePool().getSpmVdsId() != null) {
            spm = getVdsDao().get(getStoragePool().getSpmVdsId());
        }

        if (isLastMaster) {
            if (spm != null) {
                final VDSReturnValue stopSpmReturnValue = runVdsCommand(VDSCommandType.SpmStopOnIrs,
                        new SpmStopOnIrsVDSCommandParameters(getStoragePool().getId()));
                if (!stopSpmReturnValue.getSucceeded()) {
                    // no need to continue because DisconnectStoragePool will
                    // fail if host is SPM
                    log.error("Aborting execution due to failure to stop SPM");
                    setSucceeded(false);
                    return;
                }
                runVdsCommand(VDSCommandType.DisconnectStoragePool,
                        new DisconnectStoragePoolVDSCommandParameters(spm.getId(),
                                getStoragePool().getId(), spm.getVdsSpmId()));
            }
            runSynchronizeOperation(new DisconnectStoragePoolAsyncOperationFactory());
        }

        if (!getParameters().isInactive()) {
            getEventQueue().submitEventSync(
                    new Event(getParameters().getStoragePoolId(), getParameters().getStorageDomainId(), null, EventType.POOLREFRESH, ""),
                    () -> {
                        runSynchronizeOperation(new AfterDeactivateSingleAsyncOperationFactory(),
                                isLastMaster,
                                newMasterId);
                        return null;
                    });

            if (spm != null) {
                getStorageHelper(getStorageDomain()).disconnectStorageFromDomainByVdsId(getStorageDomain(), spm.getId());
            }
        }

        executeInNewTransaction(() -> {
            if (getParameters().isInactive()) {
                map.setStatus(StorageDomainStatus.Inactive);
            } else if (isLastMaster) {
                map.setStatus(StorageDomainStatus.Maintenance);
            } else {
                log.info("Domain '{}' will remain in '{}' status until deactivated on all hosts",
                        getStorageDomain().getId(), map.getStatus());
            }
            getStoragePoolIsoMapDao().updateStatus(map.getId(), map.getStatus());
            if (newMaster != null) {
                StoragePoolIsoMap mapOfNewMaster = newMaster.getStoragePoolIsoMapData();
                mapOfNewMaster.setStatus(StorageDomainStatus.Active);
                getStoragePoolIsoMapDao().updateStatus(mapOfNewMaster.getId(), mapOfNewMaster.getStatus());
            }
            return null;
        });

        if (!getParameters().isSkipChecks()) {
            notifyAsyncTasks();
        }

        setSucceeded(true);
    }

    @Override
    protected List<VDS> getAllRunningVdssInPool() {
        Set<VDSStatus> vdsStatus = EnumSet.copyOf(StoragePoolDomainHelper.vdsDomainsActiveMonitoringStatus);
        vdsStatus.addAll(StoragePoolDomainHelper.vdsDomainsMaintenanceMonitoringStatus);

        return getVdsDao().getAllForStoragePoolAndStatuses(getStoragePool().getId(), vdsStatus);
    }

    private void deactivateCinderStorageDomain() {
        List<Pair<Guid, Boolean>> hostsConnectionResults = disconnectHostsInUpToDomainStorageServer();
        for (Pair<Guid, Boolean> pair : hostsConnectionResults) {
            if (!pair.getSecond()) {
                log.error("Failed to deactivate Cinder storage domain '{}' due to secrets un-registration failure.",
                        getStorageDomain().getName());
                StoragePoolIsoMap map = getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(
                        getParameters().getStorageDomainId(), getParameters().getStoragePoolId()));
                map.setStatus(StorageDomainStatus.Inactive);
                getStoragePoolIsoMapDao().updateStatus(map.getId(), map.getStatus());
                return;
            }
        }
        CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
        CINDERStorageHelper.deactivateCinderDomain(getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
        setSucceeded(true);
    }

    /**
     * Send notification to user about tasks still running at the moment when the storage got deactivated.
     */
    private void notifyAsyncTasks() {
        final List<Guid> asyncTasks =
                getAsyncTaskDao().getAsyncTaskIdsByEntity(getParameters().getStorageDomainId());

        if (!asyncTasks.isEmpty()) {
            AuditLogableBase auditLogableBase = new AuditLogableBase();
            auditLogableBase.setStorageDomain(getStorageDomain());
            auditLogDirector.log(auditLogableBase, AuditLogType.STORAGE_DOMAIN_TASKS_ERROR);
        }
    }

    /**
     * In case of master domain this method decide if to move master to other domain or move pool to maintenance (since
     * there is no master domain)
     *
     * @param newMaster The selected new master domain
     * @param lockNewMaster If true the new master domain will be locked
     * @return true if newMaster is the last master
     */
    protected boolean proceedStorageDomainTreatmentByDomainType(final StorageDomain newMaster,
                                                                final boolean lockNewMaster) {
        if (newMaster == null) {
            return true;
        }

        newMaster.getStorageStaticData().setLastTimeUsedAsMaster(System.currentTimeMillis());

        if (newMaster.getStorageDomainType() != StorageDomainType.Master) {
            executeInNewTransaction(() -> {
                StoragePoolIsoMap newMasterMap = newMaster.getStoragePoolIsoMapData();
                getCompensationContext().snapshotEntityUpdated(newMaster.getStorageStaticData());
                newMaster.setStorageDomainType(StorageDomainType.Master);
                if (lockNewMaster) {
                    newMasterMap.setStatus(StorageDomainStatus.Unknown);
                    getCompensationContext().snapshotEntityStatus(newMasterMap);
                    newMaster.setStatus(StorageDomainStatus.Locked);
                    getStoragePoolIsoMapDao().updateStatus(newMasterMap.getId(), newMasterMap.getStatus());
                }
                updateStorageDomainStaticData(newMaster.getStorageStaticData());
                getCompensationContext().snapshotEntityUpdated(getStorageDomain().getStorageStaticData());
                getStorageDomain().setStorageDomainType(StorageDomainType.Data);
                updateStorageDomainStaticData(getStorageDomain().getStorageStaticData());
                getCompensationContext().stateChanged();
                return null;
            });
        } else {
            updateStorageDomainStaticData(newMaster.getStorageStaticData());
        }

        updateStoragePoolMasterDomainVersionInDiffTransaction();

        return false;
    }

    private void updateStorageDomainStaticData(StorageDomainStatic storageDomainStatic) {
        getStorageDomainStaticDao().update(storageDomainStatic);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getParameters().getIsInternal() ?
                getSucceeded() ? AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN
                        : AuditLogType.SYSTEM_DEACTIVATE_STORAGE_DOMAIN_FAILED
                :
                getSucceeded() ?
                        isLastMaster ?
                                AuditLogType.USER_DEACTIVATED_LAST_MASTER_STORAGE_DOMAIN :
                                AuditLogType.USER_DEACTIVATED_STORAGE_DOMAIN
                        :
                        AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        StorageDomain storageDomain = getStorageDomain();
        if (storageDomain != null) {
            Map<String, Pair<String, String>> locks = new HashMap<>();
            locks.put(storageDomain.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            if (storageDomain.getStorageDomainType() == StorageDomainType.Master) {
                locks.put(storageDomain.getStoragePoolId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
            return locks;
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        StorageDomain storageDomain = getStorageDomain();
        if (storageDomain != null && storageDomain.getStorageDomainType() == StorageDomainType.Data
                && storageDomain.getStoragePoolId() != null) {
            return Collections.singletonMap(storageDomain.getStoragePoolId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }
}
