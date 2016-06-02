package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.memory.LiveSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.MemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.memory.NullableMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.StatelessSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.VmUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.SnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
@DisableInPrepareMode
public class CreateAllSnapshotsFromVmCommand<T extends CreateAllSnapshotsFromVmParameters> extends VmCommand<T> implements QuotaStorageDependent {

    private List<DiskImage> cachedSelectedActiveDisks;
    private List<DiskImage> cachedImagesDisks;
    private Guid cachedStorageDomainId = Guid.Empty;
    private String cachedSnapshotIsBeingTakenMessage;
    private Guid newActiveSnapshotId = Guid.newGuid();
    private MemoryImageBuilder memoryBuilder;

    public CreateAllSnapshotsFromVmCommand(Guid commandId) {
        super(commandId);
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
    public void init() {
        getParameters().setUseCinderCommandCallback(isCinderDisksExist());
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
        List<Disk> disks = DbFacade.getInstance().getDiskDao().getAllForVm(getVmId());
        List<DiskImage> allDisks = new ArrayList<>(getDiskImages(disks));
        allDisks.addAll(ImagesHandler.getCinderLeafImages(disks, false));
        return allDisks;
    }

    private List<DiskImage> getDiskImages(List<Disk> disks) {
        if (cachedImagesDisks == null) {
            cachedImagesDisks = ImagesHandler.filterImageDisks(disks, true, true, true);
        }
        return cachedImagesDisks;

    }

    /**
     * Filter all allowed snapshot disks.
     * @return list of disks to be snapshot.
     */
    protected List<DiskImage> getDisksList() {
        if (cachedSelectedActiveDisks == null) {
            List<DiskImage> imagesAndCinderForVm = getDiskImagesForVm();

            // Get disks from the specified parameters or according to the VM
            if (getParameters().getDisks() == null) {
                cachedSelectedActiveDisks = imagesAndCinderForVm;
            }
            else {
                // Get selected images from 'DiskImagesForVm' to ensure disks entities integrity
                // (i.e. only images' IDs and Cinders' IDs are relevant).
                cachedSelectedActiveDisks = ImagesHandler.imagesIntersection(imagesAndCinderForVm, getParameters().getDisks());
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
        vmDisksList = ImagesHandler.getDisksDummiesForStorageAllocations(vmDisksList);
        List<DiskImage> allDisks = new ArrayList<>(vmDisksList);

        List<DiskImage> memoryDisksList = null;
        if (getParameters().isSaveMemory()) {
            memoryDisksList = MemoryUtils.createDiskDummies(VmUtils.getSnapshotMemorySizeInBytes(getVm()),
                    MemoryUtils.METADATA_SIZE_IN_BYTES);
            if (Guid.Empty.equals(getStorageDomainIdForVmMemory(memoryDisksList))) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
            }
            allDisks.addAll(memoryDisksList);
        }

        MultipleStorageDomainsValidator sdValidator = createMultipleStorageDomainsValidator(allDisks);
        if (!validate(sdValidator.allDomainsExistAndActive())
                || !validate(sdValidator.allDomainsWithinThresholds())
                || !validateCinder()) {
            return false;
        }

        if (memoryDisksList == null) { //no memory volumes
            return validate(sdValidator.allDomainsHaveSpaceForNewDisks(vmDisksList));
        }

        return validate(sdValidator.allDomainsHaveSpaceForAllDisks(vmDisksList, memoryDisksList));
    }

    public boolean validateCinder() {
        List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(DbFacade.getInstance().getDiskDao().getAllForVm(getVmId()));
        if (!cinderDisks.isEmpty()) {
            CinderDisksValidator cinderDisksValidator = getCinderDisksValidator(cinderDisks);
            return validate(cinderDisksValidator.validateCinderDiskSnapshotsLimits());
        }
        return true;
    }

    protected CinderDisksValidator getCinderDisksValidator(List<CinderDisk> cinderDisks) {
        return new CinderDisksValidator(cinderDisks);
    }

    protected MemoryImageBuilder getMemoryImageBuilder() {
        if (memoryBuilder == null) {
            memoryBuilder = createMemoryImageBuilder();
        }
        return memoryBuilder;
    }

    private void incrementVmGeneration() {
        getVmStaticDao().incrementDbGeneration(getVm().getId());
    }

    @Override
    protected void executeVmCommand() {
        Guid createdSnapshotId = updateActiveSnapshotId();
        setActionReturnValue(createdSnapshotId);
        MemoryImageBuilder memoryImageBuilder = getMemoryImageBuilder();
        freezeVm();
        createSnapshotsForDisks();
        memoryImageBuilder.build();
        addSnapshotToDB(createdSnapshotId, memoryImageBuilder);
        fastForwardDisksToActiveSnapshot();
        setSucceeded(true);
    }

    private Guid updateActiveSnapshotId() {
        final Snapshot activeSnapshot = getSnapshotDao().get(getVmId(), SnapshotType.ACTIVE);
        final Guid activeSnapshotId = activeSnapshot.getId();

        TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            getCompensationContext().snapshotEntity(activeSnapshot);
            getSnapshotDao().updateId(activeSnapshotId, newActiveSnapshotId);
            activeSnapshot.setId(newActiveSnapshotId);
            getCompensationContext().snapshotNewEntity(activeSnapshot);
            getCompensationContext().stateChanged();
            return null;
        });

        return activeSnapshotId;
    }

    public Guid getStorageDomainIdForVmMemory(List<DiskImage> memoryDisksList) {
        if (cachedStorageDomainId.equals(Guid.Empty) && getVm() != null) {
            StorageDomain storageDomain = MemoryStorageHandler.getInstance().findStorageDomainForMemory(
                    getVm().getStoragePoolId(), memoryDisksList, getDisksList(), getVm());
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
        // Reset cachedSelectedActiveDisks so new Cinder volumes can be fetched when calling getDisksList.
        cachedSelectedActiveDisks = null;
        return new SnapshotsManager().addSnapshot(snapshotId,
                getParameters().getDescription(),
                SnapshotStatus.LOCKED,
                getParameters().getSnapshotType(),
                getVm(),
                true,
                memoryImageBuilder.getVolumeStringRepresentation(),
                getDisksList(),
                getCompensationContext());
    }

    private void createSnapshotsForDisks() {
        for (DiskImage disk : getDisksList()) {
            if (disk.getDiskStorageType() == DiskStorageType.CINDER) {
                CreateCinderSnapshotParameters params = buildChildCommandParameters(disk);
                params.setQuotaId(disk.getQuotaId());

                Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                        VdcActionType.CreateCinderSnapshot,
                        params,
                        cloneContext().withoutCompensationContext().withoutLock());
                try {
                    VdcReturnValueBase vdcReturnValueBase = future.get();
                    if (!vdcReturnValueBase.getSucceeded()) {
                        log.error("Error creating snapshot for Cinder disk '{}'", disk.getDiskAlias());
                        throw new EngineException(EngineError.CINDER_ERROR, "Failed to create snapshot!");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error creating snapshot for Cinder disk '{}': {}", disk.getDiskAlias(), e.getMessage());
                    throw new EngineException(EngineError.CINDER_ERROR, "Failed to create snapshot!");
                }
                continue;
            }
            VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                    VdcActionType.CreateSnapshot,
                    buildCreateSnapshotParameters(disk),
                    ExecutionHandler.createDefaultContextForTasks(getContext()));

            if (vdcReturnValue.getSucceeded()) {
                getTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
            } else {
                throw new EngineException(vdcReturnValue.getFault().getError(),
                        "Failed to create snapshot!");
            }
        }
    }

    private CreateCinderSnapshotParameters buildChildCommandParameters(DiskImage cinderDisk) {
        CreateCinderSnapshotParameters createParams =
                new CreateCinderSnapshotParameters(((CinderDisk) getDiskDao().get(cinderDisk.getId())).getImageId());
        createParams.setVmSnapshotId(newActiveSnapshotId);
        createParams.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        createParams.setDescription(getParameters().getDescription());
        createParams.setSnapshotType(getParameters().getSnapshotType());
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        return createParams;
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
        ImagesActionsParametersBase result = new ImagesActionsParametersBase(image.getImageId());
        result.setDescription(getParameters().getDescription());
        result.setSessionId(getParameters().getSessionId());
        result.setQuotaId(image.getQuotaId());
        result.setDiskProfileId(image.getDiskProfileId());
        result.setVmSnapshotId(newActiveSnapshotId);
        result.setEntityInfo(getParameters().getEntityInfo());
        result.setParentCommand(getActionType());
        result.setParentParameters(getParameters());
        if (getParameters().getDiskIdsToIgnoreInChecks().contains(image.getId())) {
            result.setLeaveLocked(true);
        }
        return result;
    }

    private boolean shouldPerformLiveSnapshot(Snapshot snapshot) {
        return isLiveSnapshotApplicable() && snapshot != null &&
                (snapshotWithMemory(snapshot) || !getDisksList().isEmpty());
    }

    private boolean snapshotWithMemory(Snapshot snapshot) {
        return getParameters().isSaveMemory() && snapshot.containsMemory();
    }

    @Override
    protected void endVmCommand() {
        Snapshot createdSnapshot = getSnapshotDao().get(getVmId(), getParameters().getSnapshotType(), SnapshotStatus.LOCKED);
        // if the snapshot was not created in the DB
        // the command should also be handled as a failure
        boolean taskGroupSucceeded = createdSnapshot != null && getParameters().getTaskGroupSuccess();
        boolean liveSnapshotRequired = shouldPerformLiveSnapshot(createdSnapshot);
        boolean liveSnapshotSucceeded = false;

        if (taskGroupSucceeded) {
            getSnapshotDao().updateStatus(createdSnapshot.getId(), SnapshotStatus.OK);

            if (liveSnapshotRequired) {
                liveSnapshotSucceeded = performLiveSnapshot(createdSnapshot);
            } else {
                // If the created snapshot contains memory, remove the memory volumes as
                // they are not going to be in use since no live snapshot is created
                if (snapshotWithMemory(createdSnapshot)) {
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
                if (snapshotWithMemory(createdSnapshot)) {
                    removeMemoryVolumesOfSnapshot(createdSnapshot);
                }
            } else {
                log.warn("No snapshot was created for VM '{}' which is in LOCKED status", getVmId());
            }
        }

        incrementVmGeneration();
        thawVm();
        endActionOnDisks();
        setSucceeded(taskGroupSucceeded && (!liveSnapshotRequired || liveSnapshotSucceeded));
        getReturnValue().setEndActionTryAgain(false);
    }

    private void logMemorySavingFailed() {
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        auditLogDirector.log(this, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_NO_MEMORY_FAILURE);
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
            TransactionSupport.executeInScope(TransactionScopeOption.Suppress, () -> {
                runVdsCommand(VDSCommandType.Snapshot, buildLiveSnapshotParameters(snapshot));
                return null;
            });
        } catch (EngineException e) {
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

        SnapshotVDSCommandParameters parameters = new SnapshotVDSCommandParameters(
                getVm().getRunOnVds(), getVm().getId(), filteredPluggedDisks);

        if (isMemorySnapshotSupported()) {
            parameters.setMemoryVolume(snapshot.getMemoryVolume());
        }
        parameters.setVmFrozen(shouldFreezeOrThawVm());

        return parameters;
    }

    /**
     * Check if Memory Snapshot is supported
     */
    private boolean isMemorySnapshotSupported() {
        return FeatureSupported.isMemorySnapshotSupportedByArchitecture(
                        getVm().getClusterArch(), getVm().getCompatibilityVersion());
    }

    /**
     * Freezing the VM is needed for live snapshot with Cinder disks.
     */
    private void freezeVm() {
        if (!shouldFreezeOrThawVm()) {
            return;
        }

        VDSReturnValue returnValue;
        try {
            auditLogDirector.log(this, AuditLogType.FREEZE_VM_INITIATED);
            returnValue = runVdsCommand(VDSCommandType.Freeze, new VdsAndVmIDVDSParametersBase(
                    getVds().getId(), getVmId()));
        } catch (EngineException e) {
            handleFreezeVmFailure(e);
            return;
        }
        if (returnValue.getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.FREEZE_VM_SUCCESS);
        } else {
            handleFreezeVmFailure(new EngineException(EngineError.freezeErr));
        }
    }

    /**
     * VM thaw is needed if the VM was frozen.
     */
    private void thawVm() {
        if (!shouldFreezeOrThawVm()) {
            return;
        }

        VDSReturnValue returnValue;
        try {
            returnValue = runVdsCommand(VDSCommandType.Thaw, new VdsAndVmIDVDSParametersBase(
                    getVds().getId(), getVmId()));
        } catch (EngineException e) {
            handleThawVmFailure(e);
            return;
        }
        if (!returnValue.getSucceeded()) {
            handleThawVmFailure(new EngineException(EngineError.thawErr));
        }
    }

    private boolean shouldFreezeOrThawVm() {
        return isLiveSnapshotApplicable() && isCinderDisksExist();
    }

    private boolean isCinderDisksExist() {
        return !ImagesHandler.filterDisksBasedOnCinder(getDisksList()).isEmpty();
    }

    private void handleVmFailure(EngineException e, AuditLogType auditLogType, String warnMessage) {
        log.warn(warnMessage, e.getMessage());
        log.debug("Exception", e);
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        updateCallStackFromThrowable(e);
        auditLogDirector.log(this, auditLogType);
    }

    private void handleVdsLiveSnapshotFailure(EngineException e) {
        handleVmFailure(e, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_FINISHED_FAILURE,
                "Could not perform live snapshot due to error, VM will still be configured to the new created"
                        + " snapshot: {}");
    }

    private void handleFreezeVmFailure(EngineException e) {
        handleVmFailure(e, AuditLogType.FAILED_TO_FREEZE_VM,
                "Could not freeze VM guest filesystems due to an error: {}");
    }

    private void handleThawVmFailure(EngineException e) {
        handleVmFailure(e, AuditLogType.FAILED_TO_THAW_VM,
                "Could not thaw VM guest filesystems due to an error: {}");
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
    protected boolean validate() {

        if (getVm() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
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
                && validate(vmValidator.vmNotRunningStateless())
                && (!getParameters().isSaveMemory() || validate(vmValidator.vmNotHavingPciPassthroughDevices())))) {
            return false;
        }

        List<DiskImage> disksList = getDisksListForChecks();
        if (disksList.size() > 0) {
            DiskImagesValidator diskImagesValidator = createDiskImageValidator(disksList);
            if (!(validate(diskImagesValidator.diskImagesNotLocked())
                    && validate(diskImagesValidator.diskImagesNotIllegal()))) {
                return false;
            }
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
        return validate(vmValidator.vmNotSavingRestoring()) &&
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

    protected MultipleStorageDomainsValidator createMultipleStorageDomainsValidator(Collection<DiskImage> disksList) {
        return new MultipleStorageDomainsValidator(getVm().getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(disksList));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
        addValidationMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
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
            StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_SNAPSHOT_IS_BEING_TAKEN_FOR_VM.name());
            if (getVmName() != null) {
                builder.append(String.format("$VmName %1$s", getVmName()));
            }
            cachedSnapshotIsBeingTakenMessage = builder.toString();
        }
        return cachedSnapshotIsBeingTakenMessage;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
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

    @Override
    public CommandCallback getCallback() {
        return new ConcurrentChildCommandsExecutionCallback();
    }
}
