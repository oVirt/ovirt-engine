package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.memory.LiveSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.MemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.memory.NullableMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.StatelessSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.validator.LiveSnapshotValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.SnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
@DisableInPrepareMode
public class CreateAllSnapshotsFromVmCommand<T extends CreateAllSnapshotsFromVmParameters> extends VmCommand<T> implements QuotaStorageDependent, TaskHandlerCommand<CreateAllSnapshotsFromVmParameters> {

    private List<DiskImage> cachedSelectedActiveDisks;
    private Guid cachedStorageDomainId = Guid.Empty;
    private String cachedSnapshotIsBeingTakenMessage;
    private Guid newActiveSnapshotId = Guid.newGuid();
    private MemoryImageBuilder memoryBuilder;

    protected CreateAllSnapshotsFromVmCommand(Guid commandId) {
        super(commandId);
    }

    public CreateAllSnapshotsFromVmCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    public CreateAllSnapshotsFromVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
        setSnapshotName(parameters.getDescription());
        setStoragePoolId(getVm() != null ? getVm().getStoragePoolId() : null);
    }


    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), getParameters().getDescription());
        }
        return jobProperties;
    }


    private List<DiskImage> getDiskImagesForVm() {
        return ImagesHandler.filterImageDisks(DbFacade.getInstance().getDiskDao().getAllForVm(getVmId()), true, true, true);
    }

    /**
     * Filter all allowed snapshot disks.
     * @return list of disks to be snapshot.
     */
    protected List<DiskImage> getDisksList() {
        if (cachedSelectedActiveDisks == null) {
            List<DiskImage> imagesForVm = getDiskImagesForVm();

            // Get disks from the specified parameters or according to the VM
            if (getParameters().getDisks() == null) {
                cachedSelectedActiveDisks = imagesForVm;
            }
            else {
                // Get selected images from 'DiskImagesForVm' to ensure disks entities integrity
                // (i.e. only images' IDs are relevant).
                cachedSelectedActiveDisks = ImagesHandler.imagesIntersection(imagesForVm, getParameters().getDisks());
            }
        }
        return cachedSelectedActiveDisks;
    }

    protected List<DiskImage> getDisksListForChecks() {
        List<DiskImage> disksListForChecks = getDisksList();
        if (getParameters().getDiskIdsToIgnoreInChecks().isEmpty()) {
            return disksListForChecks;
        }

        List<DiskImage> toReturn = new LinkedList<>();
        for (DiskImage diskImage : disksListForChecks) {
            if (!getParameters().getDiskIdsToIgnoreInChecks().contains(diskImage.getId())) {
                toReturn.add(diskImage);
            }
        }

        return toReturn;
    }

    private boolean validateStorage() {
        List<DiskImage> vmDisksList = getDisksListForChecks();
        if (vmDisksList.size() > 0) {
            DiskImagesValidator diskImagesValidator = createDiskImageValidator(vmDisksList);
            if (!(validate(diskImagesValidator.diskImagesNotLocked())
                    && validate(diskImagesValidator.diskImagesNotIllegal()))) {
                return false;
            }
        }
        vmDisksList = ImagesHandler.getDisksDummiesForStorageAllocations(vmDisksList);
        List<DiskImage> allDisks = new ArrayList<>(vmDisksList);

        List<DiskImage> memoryDisksList = null;
        if (getParameters().isSaveMemory()) {
            memoryDisksList = MemoryUtils.createDiskDummies(getVm().getTotalMemorySizeInBytes(), MemoryUtils.META_DATA_SIZE_IN_BYTES);
            if (Guid.Empty.equals(getStorageDomainIdForVmMemory(memoryDisksList))) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
            }
            allDisks.addAll(memoryDisksList);
        }

        MultipleStorageDomainsValidator sdValidator = createMultipleStorageDomainsValidator(allDisks);
        if (!validate(sdValidator.allDomainsExistAndActive())
                || !validate(sdValidator.allDomainsWithinThresholds())) {
            return false;
        }

        if (memoryDisksList == null) { //no memory volumes
            return validate(sdValidator.allDomainsHaveSpaceForNewDisks(vmDisksList));
        }

        return validate(sdValidator.allDomainsHaveSpaceForAllDisks(vmDisksList, memoryDisksList));
    }

    protected MemoryImageBuilder getMemoryImageBuilder() {
        if (memoryBuilder == null) {
            memoryBuilder = createMemoryImageBuilder();
        }
        return memoryBuilder;
    }

    private void incrementVmGeneration() {
        getVmStaticDAO().incrementDbGeneration(getVm().getId());
    }

    @Override
    protected void executeVmCommand() {
        Guid createdSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        getParameters().setSnapshotType(determineSnapshotType());

        getSnapshotDao().updateId(createdSnapshotId, newActiveSnapshotId);

        setActionReturnValue(createdSnapshotId);

        MemoryImageBuilder memoryImageBuilder = getMemoryImageBuilder();
        addSnapshotToDB(createdSnapshotId, memoryImageBuilder);
        createSnapshotsForDisks();
        fastForwardDisksToActiveSnapshot();
        memoryImageBuilder.build();

        if (getTaskIdList().isEmpty()) {
            getParameters().setTaskGroupSuccess(true);
            incrementVmGeneration();
        }

        setSucceeded(true);
    }

    public Guid getStorageDomainIdForVmMemory(List<DiskImage> memoryDisksList) {
        if (cachedStorageDomainId.equals(Guid.Empty) && getVm() != null) {
            StorageDomain storageDomain = VmHandler.findStorageDomainForMemory(getVm().getStoragePoolId(), memoryDisksList);
            if (storageDomain != null) {
                cachedStorageDomainId = storageDomain.getId();
            }
        }
        return cachedStorageDomainId;
    }

    private MemoryImageBuilder createMemoryImageBuilder() {
        if (!isMemorySnapshotSupported()) {
            return new NullableMemoryImageBuilder();
        }

        if (getParameters().getSnapshotType() == SnapshotType.STATELESS) {
            return new StatelessSnapshotMemoryImageBuilder(getVm());
        }

        if (getParameters().isSaveMemory() && isLiveSnapshotApplicable()) {
            return new LiveSnapshotMemoryImageBuilder(getVm(), cachedStorageDomainId, getStoragePool(), this);
        }

        return new NullableMemoryImageBuilder();
    }

    private Snapshot addSnapshotToDB(Guid snapshotId, MemoryImageBuilder memoryImageBuilder) {
        boolean taskExists = !getDisksList().isEmpty() || memoryImageBuilder.isCreateTasks();
        return new SnapshotsManager().addSnapshot(snapshotId,
                getParameters().getDescription(),
                taskExists ? SnapshotStatus.LOCKED : SnapshotStatus.OK,
                getParameters().getSnapshotType(),
                getVm(),
                true,
                memoryImageBuilder.getVolumeStringRepresentation(),
                getDisksList(),
                getCompensationContext());
    }

    private void createSnapshotsForDisks() {
        for (DiskImage image : getDisksList()) {
            VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                    VdcActionType.CreateSnapshot,
                    buildCreateSnapshotParameters(image),
                    ExecutionHandler.createDefaultContextForTasks(getContext()));

            if (vdcReturnValue.getSucceeded()) {
                getTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
            } else {
                throw new VdcBLLException(vdcReturnValue.getFault().getError(),
                        "Failed to create snapshot!");
            }
        }
    }

    private void fastForwardDisksToActiveSnapshot() {
        if (getParameters().getDisks() != null) {
            // Remove disks included in snapshot
            List<DiskImage> diskImagesToUpdate = ImagesHandler.imagesSubtract(getDiskImagesForVm(), getParameters().getDisks());

            // Fast-forward non-included disks to active snapshot
            for (DiskImage diskImage : diskImagesToUpdate) {
                getImageDao().updateImageVmSnapshotId(diskImage.getImageId(), newActiveSnapshotId);
            }
        }
    }

    private ImagesActionsParametersBase buildCreateSnapshotParameters(DiskImage image) {
        VdcActionType parentCommand = getParameters().getParentCommand() != VdcActionType.Unknown ?
                getParameters().getParentCommand() : VdcActionType.CreateAllSnapshotsFromVm;

        ImagesActionsParametersBase result = new ImagesActionsParametersBase(image.getImageId());
        result.setDescription(getParameters().getDescription());
        result.setSessionId(getParameters().getSessionId());
        result.setQuotaId(image.getQuotaId());
        result.setDiskProfileId(image.getDiskProfileId());
        result.setVmSnapshotId(newActiveSnapshotId);
        result.setEntityInfo(getParameters().getEntityInfo());
        result.setParentCommand(parentCommand);
        result.setParentParameters(getParametersForTask(parentCommand, getParameters()));
        if (getParameters().getDiskIdsToIgnoreInChecks().contains(image.getId())) {
            result.setLeaveLocked(true);
        }
        return result;
    }

    /**
     * @return For internal execution, return the type from parameters, otherwise return {@link SnapshotType#REGULAR}.
     */
    protected SnapshotType determineSnapshotType() {
        return isInternalExecution() ? getParameters().getSnapshotType() : SnapshotType.REGULAR;
    }

    @Override
    protected void endVmCommand() {
        Snapshot createdSnapshot = getSnapshotDao().get(getVmId(), getParameters().getSnapshotType(), SnapshotStatus.LOCKED);
        // if the snapshot was not created in the DB
        // the command should also be handled as a failure
        boolean taskGroupSucceeded = createdSnapshot != null && getParameters().getTaskGroupSuccess();
        boolean liveSnapshotRequired = isLiveSnapshotApplicable();
        boolean liveSnapshotSucceeded = false;

        if (taskGroupSucceeded) {
            getSnapshotDao().updateStatus(createdSnapshot.getId(), SnapshotStatus.OK);

            if (liveSnapshotRequired) {
                liveSnapshotSucceeded = performLiveSnapshot(createdSnapshot);
            } else {
                // If the created snapshot contains memory, remove the memory volumes as
                // they are not going to be in use since no live snapshot is created
                if (getParameters().isSaveMemory() && createdSnapshot.containsMemory()) {
                    logMemorySavingFailed();
                    getSnapshotDao().removeMemoryFromSnapshot(createdSnapshot.getId());
                    removeMemoryVolumesOfSnapshot(createdSnapshot);
                }
            }
        } else {
            if (createdSnapshot != null) {
                revertToActiveSnapshot(createdSnapshot.getId());
                // If the removed snapshot contained memory, remove the memory volumes
                // Note that the memory volumes might not have been created
                if (getParameters().isSaveMemory() && createdSnapshot.containsMemory()) {
                    removeMemoryVolumesOfSnapshot(createdSnapshot);
                }
            } else {
                log.warn("No snapshot was created for VM '{}' which is in LOCKED status", getVmId());
            }
        }

        incrementVmGeneration();

        endActionOnDisks();
        setSucceeded(taskGroupSucceeded && (!liveSnapshotRequired || liveSnapshotSucceeded));
        getReturnValue().setEndActionTryAgain(false);
    }

    private void logMemorySavingFailed() {
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        AuditLogDirector.log(this, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_NO_MEMORY_FAILURE);
    }

    private void removeMemoryVolumesOfSnapshot(Snapshot snapshot) {
        VdcReturnValueBase retVal = runInternalAction(
                VdcActionType.RemoveMemoryVolumes,
                new RemoveMemoryVolumesParameters(snapshot.getMemoryVolume(), getVmId()), cloneContextAndDetachFromParent());

        if (!retVal.getSucceeded()) {
            log.error("Failed to remove memory volumes of snapshot '{}' ({})",
                    snapshot.getDescription(), snapshot.getId());
        }
    }

    protected boolean isLiveSnapshotApplicable() {
        return getParameters().getParentCommand() != VdcActionType.RunVm && getVm() != null
                && (getVm().isRunning() || getVm().getStatus() == VMStatus.Paused) && getVm().getRunOnVds() != null;
    }

    @Override
    protected List<VdcActionParametersBase> getParametersForChildCommand() {
        List<VdcActionParametersBase> sortedList = getParameters().getImagesParameters();
        Collections.sort(sortedList, new Comparator<VdcActionParametersBase>() {
            @Override
            public int compare(VdcActionParametersBase o1, VdcActionParametersBase o2) {
                if (o1 instanceof ImagesActionsParametersBase && o2 instanceof ImagesActionsParametersBase) {
                    return ((ImagesActionsParametersBase) o1).getDestinationImageId()
                            .compareTo(((ImagesActionsParametersBase) o2).getDestinationImageId());
                }
                return 0;
            }
        });

        return sortedList;
    }

    /**
     * Perform live snapshot on the host that the VM is running on. If the snapshot fails, and the error is
     * unrecoverable then the {@link CreateAllSnapshotsFromVmParameters#getTaskGroupSuccess()} will return false.
     *
     * @param snapshot
     *            Snapshot to revert to being active, in case of rollback.
     */
    protected boolean performLiveSnapshot(final Snapshot snapshot) {
        try {
            TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    runVdsCommand(VDSCommandType.Snapshot, buildLiveSnapshotParameters(snapshot));
                    return null;
                }
            });
        } catch (VdcBLLException e) {
            handleVdsLiveSnapshotFailure(e);
            return false;
        }
        return true;
    }

    private SnapshotVDSCommandParameters buildLiveSnapshotParameters(Snapshot snapshot) {
        List<Disk> pluggedDisksForVm = getDiskDao().getAllForVm(getVm().getId(), true);
        List<DiskImage> filteredPluggedDisksForVm = ImagesHandler.filterImageDisks(pluggedDisksForVm, false, true, true);

        // 'filteredPluggedDisks' should contain only disks from 'getDisksList()' that are plugged to the VM.
        List<DiskImage> filteredPluggedDisks = ImagesHandler.imagesIntersection(filteredPluggedDisksForVm, getDisksList());

        if (isMemorySnapshotSupported()) {
            return new SnapshotVDSCommandParameters(getVm().getRunOnVds(),
                    getVm().getId(),
                    filteredPluggedDisks,
                    snapshot.getMemoryVolume());
        }
        else {
            return new SnapshotVDSCommandParameters(getVm().getRunOnVds(),
                    getVm().getId(),
                    filteredPluggedDisks);
        }
    }

    /**
     * Check if Memory Snapshot is supported
     * @return
     */
    private boolean isMemorySnapshotSupported () {
        return FeatureSupported.memorySnapshot(getVm().getVdsGroupCompatibilityVersion())
                && FeatureSupported.isMemorySnapshotSupportedByArchitecture(getVm().getClusterArch(), getVm().getVdsGroupCompatibilityVersion());
    }

    private void handleVdsLiveSnapshotFailure(VdcBLLException e) {
        log.warn("Could not perform live snapshot due to error, VM will still be configured to the new created"
                        + " snapshot: {}",
                e.getMessage());
        log.debug("Exception", e);
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        updateCallStackFromThrowable(e);
        AuditLogDirector.log(this, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_FINISHED_FAILURE);
    }

    /**
     * Return the given snapshot ID's snapshot to be the active snapshot. The snapshot with the given ID is removed
     * in the process.
     *
     * @param createdSnapshotId
     *            The snapshot ID to return to being active.
     */
    protected void revertToActiveSnapshot(Guid createdSnapshotId) {
        if (createdSnapshotId != null) {
            getSnapshotDao().remove(createdSnapshotId);
            getSnapshotDao().updateId(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE), createdSnapshotId);
        }
        setSucceeded(false);
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT : AuditLogType.USER_FAILED_CREATE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {

        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!isSpecifiedDisksExist(getParameters().getDisks())) {
            return false;
        }

        // Initialize validators.
        VmValidator vmValidator = createVmValidator();
        SnapshotsValidator snapshotValidator = createSnapshotValidator();
        StoragePoolValidator spValidator = createStoragePoolValidator();

        if (!(validateVM(vmValidator) && validate(spValidator.isUp())
                && validate(vmValidator.vmNotIlegal())
                && validate(vmValidator.vmNotLocked())
                && validate(snapshotValidator.vmNotDuringSnapshot(getVmId()))
                && validate(snapshotValidator.vmNotInPreview(getVmId()))
                && validate(vmValidator.vmNotDuringMigration())
                && validate(vmValidator.vmNotRunningStateless()))) {
            return false;
        }

        return validateStorage();
    }

    protected StoragePoolValidator createStoragePoolValidator() {
        return new StoragePoolValidator(getStoragePool());
    }

    protected SnapshotsValidator createSnapshotValidator() {
        return new SnapshotsValidator();
    }

    protected DiskImagesValidator createDiskImageValidator(List<DiskImage> disksList) {
        return new DiskImagesValidator(disksList);
    }

    protected VmValidator createVmValidator() {
        return new VmValidator(getVm());
    }

    protected boolean validateVM(VmValidator vmValidator) {
        LiveSnapshotValidator validator = new LiveSnapshotValidator(getStoragePool().getCompatibilityVersion(), getVds());
        return (getVm().isDown() || validate(validator.canDoSnapshot())) &&
                validate(vmValidator.vmNotSavingRestoring()) &&
                validate(vmValidator.validateVmStatusUsingMatrix(VdcActionType.CreateAllSnapshotsFromVm));
    }

    private boolean isSpecifiedDisksExist(List<DiskImage> disks) {
        if (disks == null || disks.isEmpty()) {
            return true;
        }

        DiskImagesValidator diskImagesValidator = createDiskImageValidator(disks);
        if (!validate(diskImagesValidator.diskImagesNotExist())) {
            return false;
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.CreateSnapshot;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getParameters().isNeedsLocking() ?
                Collections.singletonMap(getVmId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getSnapshotIsBeingTakenForVmMessage()))
                : null;
    }

    private String getSnapshotIsBeingTakenForVmMessage() {
        if (cachedSnapshotIsBeingTakenMessage == null) {
            StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_SNAPSHOT_IS_BEING_TAKEN_FOR_VM.name());
            if (getVmName() != null) {
                builder.append(String.format("$VmName %1$s", getVmName()));
            }
            cachedSnapshotIsBeingTakenMessage = builder.toString();
        }
        return cachedSnapshotIsBeingTakenMessage;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        for (DiskImage disk : getDisksList()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    disk.getStorageIds().get(0),
                    disk.getActualSize()));
        }

        return list;
    }

    /////////////////////////////////////////
    /// TaskHandlerCommand implementation ///
    /////////////////////////////////////////

    public T getParameters() {
        return super.getParameters();
    }

    public VdcActionType getActionType() {
        return super.getActionType();
    }

    public VdcReturnValueBase getReturnValue() {
        return super.getReturnValue();
    }

    public void preventRollback() {
        throw new NotImplementedException();
    }

    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTask(taskId,
                asyncTaskCreationInfo, parentCommand,
                entityType, entityIds);
    }

    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    @Override
    public void taskEndSuccessfully() {
        // Not implemented
    }

    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }

}
