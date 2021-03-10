package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.ManagedBlockStorageHelper;
import org.ovirt.engine.core.bll.storage.pool.AfterDeactivateSingleAsyncOperationFactory;
import org.ovirt.engine.core.bll.storage.pool.DisconnectStoragePoolAsyncOperationFactory;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
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
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.constants.StorageConstants;
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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.CommandEntityDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.vdsbroker.irsbroker.SpmStopOnIrsVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class DeactivateStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private CommandEntityDao commandEntityDao;
    @Inject
    private AsyncTaskDao asyncTaskDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private CINDERStorageHelper cinderStorageHelper;
    @Inject
    private ManagedBlockStorageHelper managedBlockStorageHelper;
    @Inject
    private ImageTransferDao imageTransferDao;

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

        if (!validateDomainStatus()) {
            return false;
        }

        if (!validateNoActiveUploadsDownloads()) {
            return false;
        }

        if (!isSupportedByManagedBlockStorageDomain(getStorageDomain())) {
            return false;
        }

        if (!getParameters().getIsInternal()) {
            if (getStorageDomain().isHostedEngineStorage()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_STORAGE);
            }

            if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master
                    && !validateMasterDeactivationAllowed()) {
                return false;
            }

            if (!isNoRunningVmsWithLeasesExist()) {
                return false;
            }

            if (!isNoRunningVmsWithIsoAttached()) {
                return false;
            }

            if (!vmDao.getAllActiveForStorageDomain(getStorageDomain().getId()).isEmpty()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DETECTED_ACTIVE_VMS);
            }
        }
        if (getStoragePool().getSpmVdsId() != null) {
            // In case there are running tasks in the pool, it is impossible to deactivate the master storage domain
            List<AsyncTask> asyncTasks;
            if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master &&
                    (asyncTasks = asyncTaskDao.getAsyncTaskIdsByStoragePoolId(getStorageDomain().getStoragePoolId())).size() > 0) {
                logRunningAsyncTasks(asyncTasks);
                return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_MASTER_DOMAIN_WITH_TASKS_ON_POOL,
                        String.format("$%s %s", "tasksNames", getTaskActionsFromList(asyncTasks)));
            } else if (getStorageDomain().getStorageDomainType() != StorageDomainType.ISO &&
                    !getParameters().getIsInternal()
                    && ((asyncTasks = asyncTaskDao.getTasksByEntity(getParameters().getStorageDomainId())).size() > 0 ||
                    commandEntityDao.getCommandIdsByEntity(getParameters().getStorageDomainId()).size() > 0)) {
                logRunningAsyncTasks(asyncTasks);
                return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_TASKS,
                        String.format("$%s %s", "tasksNames", getTaskActionsFromList(asyncTasks)));
            }
        }
        return true;
    }

    private String getTaskActionsFromList(List<AsyncTask> tasks) {
        return tasks.stream()
                .filter(t -> t.getActionType() != null)
                .map(t -> t.getActionType().toString())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    protected boolean validateNoActiveUploadsDownloads() {
        for (ImageTransfer transfer : imageTransferDao.getByStorageId(getStorageDomainId())) {
            ImageTransferPhase imageTransferPhase = transfer.getPhase();
            switch (imageTransferPhase) {
                case TRANSFERRING:
                case RESUMING:
                case FINALIZING_SUCCESS:
                case FINALIZING_FAILURE:
                case FINALIZING_CLEANUP:
                    return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_DURING_UPLOAD_OR_DOWNLOAD);
            }
        }
        return true;
    }

    protected boolean validateDomainStatus() {
        // Internal execution means that the domain status is being set to Inactive - therefore it's applicable for
        // the Active/Unknown statuses.
        // On user initiated execution, we allow to deactivate domain which is monitored.
        if (!((getParameters().getIsInternal() && checkStorageDomainStatus(StorageDomainStatus.Active,
                StorageDomainStatus.Unknown))
                || checkStorageDomainStatus(StorageConstants.monitoredDomainStatuses))) {
            return false;
        }

        return true;
    }

    private boolean isNoRunningVmsWithLeasesExist() {
        List<String> runningVmsWithLeases = vmStaticDao.getAllRunningNamesWithLeaseOnStorageDomain(getStorageDomain().getId());
        if (!runningVmsWithLeases.isEmpty()) {
            String vmNames = String.join(", ", runningVmsWithLeases);
            return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_RUNNING_VMS_WITH_LEASES,
                    String.format("$vmNames %s", vmNames));
        }
        return true;
    }

    private boolean validateMasterDeactivationAllowed() {
        List<StorageDomain> domains =
                storageDomainDao.getAllForStoragePool(getStorageDomain().getStoragePoolId());

        List<StorageDomain> activeDomains = filterActiveDomains(domains);

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
        return true;
    }

    protected boolean isNoRunningVmsWithIsoAttached() {
        List<String> vmNames = getStorageDomain().getStorageDomainType() == StorageDomainType.ISO ?
                 getVmsWithAttachedISO() : vmStaticDao.getAllRunningNamesWithIsoOnStorageDomain(getStorageDomainId());
        if (!vmNames.isEmpty()) {
            return failValidation(EngineMessage.ERROR_CANNOT_DEACTIVATE_STORAGE_DOMAIN_WITH_ISO_ATTACHED,
                    String.format("$VmNames %1$s", String.join(",", vmNames)));
        }
        return true;
    }

    protected List<String> getVmsWithAttachedISO() {
        List<VmStatic> vms = vmStaticDao.getAllByStoragePoolId(getStorageDomain().getStoragePoolId());
        List<String> vmNames = new LinkedList<>();
        for (VmStatic vmStatic : vms) {
            VmDynamic vmDynamic = vmDynamicDao.get(vmStatic.getId());
            if (vmDynamic.getStatus() != VMStatus.Down && !StringUtils.isEmpty(vmDynamic.getCurrentCd())) {
                vmNames.add(vmStatic.getName());
            }
        }
        return vmNames;
    }

    /**
     * Filter the active domains excluding the domain which is the parameter for this command from the given domains list.
     *
     * @param domains
     *            The domains to filter.
     *
     * @return The active domains in the list excluding the current domain of the command.
     */
    private List<StorageDomain> filterActiveDomains(List<StorageDomain> domains) {
        return domains.stream()
                .filter(d -> d.getStatus() == StorageDomainStatus.Active && !d.getId().equals(getStorageDomain().getId()))
                .collect(Collectors.toList());
    }

    @Override
    protected void executeCommand() {
        switch (getStorageDomain().getStorageType()) {
            case CINDER:
                deactivateCinderStorageDomain();
                break;
            case MANAGED_BLOCK_STORAGE:
                deactivateManagedBlockStorageDomain();
                break;
            default:
                deactivateStorageDomain();
                break;
        }
    }

    private void deactivateStorageDomain() {
        StorageDomainStatus lastStatus = getStorageDomain().getStatus();
        final StoragePoolIsoMap map =
                storagePoolIsoMapDao.get
                        (new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
        map.setStatus(StorageDomainStatus.Unknown);
        changeStorageDomainStatusInTransaction(map,
                getParameters().isInactive() ? StorageDomainStatus.Locked : StorageDomainStatus.PreparingForMaintenance);

        final StorageDomain newMaster;

        boolean isMaster = getStorageDomain().getStorageDomainType() == StorageDomainType.Master;
        if (isMaster) {
            newMaster = electNewMaster();

        } else {
            newMaster = null;
        }

        final Guid newMasterId = newMaster != null ? newMaster.getId() : Guid.Empty;

        boolean deactivateSucceeded = true;
        if (!getParameters().isInactive()) {
            if (isMaster) {
                updateStoragePoolMasterDomainVersionInDiffTransaction();
            }
            try {
                deactivateSucceeded = runVdsCommand(VDSCommandType.DeactivateStorageDomain,
                        new DeactivateStorageDomainVDSCommandParameters(getStoragePool().getId(),
                                getStorageDomain().getId(),
                                newMasterId,
                                getStoragePool().getMasterDomainVersion())).getSucceeded();
            } catch (Exception e) {
                log.error("DeactivateStorageDomainVDS failed '{}'", getParameters().getStorageDomainId(), e);
                deactivateSucceeded = false;
            }
        } else {
            log.info("DeactivateStorageDomainVDS is skipped '{}'", getParameters().getStorageDomainId());
        }

        if (deactivateSucceeded) {
            isLastMaster = isMaster ? proceedStorageDomainTreatmentByDomainType(newMaster, true) : false;
            if (isLastMaster) {
                executeInNewTransaction(() -> {
                    getCompensationContext().snapshotEntityStatus(getStoragePool());
                    getStoragePool().setStatus(StoragePoolStatus.Maintenance);
                    storagePoolDao.updateStatus(getStoragePool().getId(), getStoragePool().getStatus());
                    getCompensationContext().stateChanged();
                    return null;
                });

                storagePoolStatusHandler.poolStatusChanged(getStoragePool().getId(), getStoragePool().getStatus());
                getStorageDomain().getStorageDynamicData().setAvailableDiskSize(null);
                getStorageDomain().getStorageDynamicData().setUsedDiskSize(null);
            }
        }
        freeLock();

        if (!deactivateSucceeded) {
            log.error("Failed to deactivate storage domain '{}'", getParameters().getStorageDomainId());
            changeStorageDomainStatusInTransaction(map, lastStatus);
            setSucceeded(false);
            return;
        }
        VDS spm = null;
        if (getStoragePool().getSpmVdsId() != null) {
            spm = vdsDao.get(getStoragePool().getSpmVdsId());
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

            if (!isLastMaster && spm != null) {
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
            storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());
            if (newMaster != null) {
                StoragePoolIsoMap mapOfNewMaster = newMaster.getStoragePoolIsoMapData();
                mapOfNewMaster.setStatus(StorageDomainStatus.Active);
                storagePoolIsoMapDao.updateStatus(mapOfNewMaster.getId(), mapOfNewMaster.getStatus());
            }
            return null;
        });

        if (!getParameters().isSkipChecks()) {
            notifyAsyncTasks();
        }

        setSucceeded(true);
    }

    private void deactivateManagedBlockStorageDomain() {
        List<Pair<Guid, Boolean>> hostsConnectionResults = disconnectHostsInUpToDomainStorageServer();
        for (Pair<Guid, Boolean> pair : hostsConnectionResults) {
            if (!pair.getSecond()) {
                log.error("Failed to deactivate Managed block storage domain '{}'", getStorageDomain().getName());
                managedBlockStorageHelper.setManagedBlockStorageInactive(getParameters().getStorageDomainId(),
                        getParameters().getStoragePoolId());
                return;
            }
        }
        managedBlockStorageHelper.deactivateManagedBlockDomain(getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
        setSucceeded(true);
    }

    @Override
    protected List<VDS> getAllRunningVdssInPool() {
        Set<VDSStatus> vdsStatus = EnumSet.copyOf(StoragePoolDomainHelper.vdsDomainsActiveMonitoringStatus);
        vdsStatus.addAll(StoragePoolDomainHelper.vdsDomainsMaintenanceMonitoringStatus);

        return vdsDao.getAllForStoragePoolAndStatuses(getStoragePool().getId(), vdsStatus);
    }

    private void deactivateCinderStorageDomain() {
        List<Pair<Guid, Boolean>> hostsConnectionResults = disconnectHostsInUpToDomainStorageServer();
        for (Pair<Guid, Boolean> pair : hostsConnectionResults) {
            if (!pair.getSecond()) {
                log.error("Failed to deactivate Cinder storage domain '{}' due to secrets un-registration failure.",
                        getStorageDomain().getName());
                StoragePoolIsoMap map = storagePoolIsoMapDao.get(new StoragePoolIsoMapId(
                        getParameters().getStorageDomainId(), getParameters().getStoragePoolId()));
                map.setStatus(StorageDomainStatus.Inactive);
                storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());
                return;
            }
        }
        cinderStorageHelper.deactivateCinderDomain(getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
        setSucceeded(true);
    }

    /**
     * Send notification to user about tasks still running at the moment when the storage got deactivated.
     */
    private void notifyAsyncTasks() {
        final List<Guid> asyncTasks =
                asyncTaskDao.getAsyncTaskIdsByEntity(getParameters().getStorageDomainId());

        if (!asyncTasks.isEmpty()) {
            auditLogDirector.log(this, AuditLogType.STORAGE_DOMAIN_TASKS_ERROR);
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
                    storagePoolIsoMapDao.updateStatus(newMasterMap.getId(), newMasterMap.getStatus());
                }
                updateStorageDomainStaticData(newMaster.getStorageStaticData());
                // Not having a master storage domain may result in an arbitrary storage domain selected as the master,
                // and we do not want to override its type.
                if (!newMaster.getId().equals(getStorageDomain().getId())) {
                    getCompensationContext().snapshotEntityUpdated(getStorageDomain().getStorageStaticData());
                    getStorageDomain().setStorageDomainType(StorageDomainType.Data);
                    updateStorageDomainStaticData(getStorageDomain().getStorageStaticData());
                }
                getCompensationContext().stateChanged();
                return null;
            });
        } else {
            updateStorageDomainStaticData(newMaster.getStorageStaticData());
        }

        return false;
    }

    private void updateStorageDomainStaticData(StorageDomainStatic storageDomainStatic) {
        storageDomainStaticDao.update(storageDomainStatic);
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

    private void logRunningAsyncTasks(List<AsyncTask> asyncTasks) {
        String runningTasks = asyncTasks
                .stream()
                .map(AsyncTask::toString)
                .collect(Collectors.joining("\n"));
        log.warn("There are running tasks: '{}'", runningTasks);
    }
}
