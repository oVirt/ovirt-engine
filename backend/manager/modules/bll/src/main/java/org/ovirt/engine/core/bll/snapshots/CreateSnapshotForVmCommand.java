package org.ovirt.engine.core.bll.snapshots;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.memory.LiveSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.MemoryDisks;
import org.ovirt.engine.core.bll.memory.MemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.memory.NullableMemoryImageBuilder;
import org.ovirt.engine.core.bll.memory.StatelessSnapshotMemoryImageBuilder;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskExistenceValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotDiskParameters;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class CreateSnapshotForVmCommand<T extends CreateSnapshotForVmParameters> extends VmCommand<T> implements SerialChildExecutingCommand, QuotaStorageDependent {
    private Guid cachedStorageDomainId = Guid.Empty;
    private Guid newActiveSnapshotId = Guid.newGuid();
    private MemoryImageBuilder memoryBuilder;
    private String cachedSnapshotIsBeingTakenMessage;
    private List<DiskImage> cachedSelectedActiveDisks;
    private List<DiskImage> cachedImagesDisks;
    private boolean isLiveSnapshotApplicable;
    private boolean isMemorySnapshotSupported;
    private boolean shouldFreezeOrThaw;

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private VmOverheadCalculator vmOverheadCalculator;
    @Inject
    private DiskDao diskDao;
    @Inject
    private MemoryStorageHandler memoryStorageHandler;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public CreateSnapshotForVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void init() {
        getParameters().setUseCinderCommandCallback(diskOfTypeExists(DiskStorageType.CINDER));
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
        setSnapshotName(getParameters().getDescription());
        setStoragePoolId(getVm() != null ? getVm().getStoragePoolId() : null);
        isMemorySnapshotSupported = isMemorySnapshotSupported();
        isLiveSnapshotApplicable = isLiveSnapshotApplicable();
        shouldFreezeOrThaw = shouldFreezeOrThawVm();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        for (DiskImage disk : getDisksList()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    disk.getStorageIds().get(0),
                    disk.getActualSize()));
        }

        return list;
    }

    @Override
    protected void executeVmCommand() {
        Guid createdSnapshotId = updateActiveSnapshotId();
        setActionReturnValue(createdSnapshotId);
        getParameters().setCreatedSnapshotId(createdSnapshotId);
        MemoryImageBuilder memoryImageBuilder = getMemoryImageBuilder();
        freezeVm();
        ActionReturnValue actionReturnValue = createSnapshotsForDisks();
        if (actionReturnValue.getSucceeded()) {
            memoryImageBuilder.build();
            addSnapshotToDB(createdSnapshotId, memoryImageBuilder);
            fastForwardDisksToActiveSnapshot();
            getParameters().setCreateSnapshotStage(CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_VOLUME_FINISHED);
            setSucceeded(true);
        }
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getCreateSnapshotStage() == CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_VOLUME_FINISHED) {
            getParameters().setCreateSnapshotStage(CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_SNAPSHOT_STARTED);
            Snapshot createdSnapshot = snapshotDao.get(getParameters().getCreatedSnapshotId());
            // if the snapshot was not created in the DB
            // the command should also be handled as a failure
            getParameters().setTaskGroupSuccess(createdSnapshot != null && getParameters().getTaskGroupSuccess());
            if (getParameters().getTaskGroupSuccess()) {
                getParameters().setLiveSnapshotRequired(shouldPerformLiveSnapshot(createdSnapshot));

                if (getParameters().isLiveSnapshotRequired()) {
                    getParameters().setLiveSnapshotSucceeded(performLiveSnapshot(createdSnapshot));
                } else if (snapshotWithMemory(createdSnapshot)) {
                    logMemorySavingFailed();
                    snapshotDao.removeMemoryFromSnapshot(createdSnapshot.getId());
                    removeMemoryVolumesOfSnapshot(createdSnapshot);
                }
            }

            getParameters().setCreateSnapshotStage(CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_SNAPSHOT_COMPLETED);
            persistCommandIfNeeded();
            return true;
        }

        return false;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), getParameters().getDescription());
        }
        return jobProperties;
    }

    public Guid getStorageDomainIdForVmMemory(MemoryDisks memoryDisks) {
        if (cachedStorageDomainId.equals(Guid.Empty) && getVm() != null) {
            StorageDomain storageDomain = memoryStorageHandler.findStorageDomainForMemory(
                    getVm().getStoragePoolId(), memoryDisks, getDisksList(), getVm());
            if (storageDomain != null) {
                cachedStorageDomainId = storageDomain.getId();
            }
        }
        return cachedStorageDomainId;
    }

    public boolean validateCinder() {
        List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(diskDao.getAllForVm(getVmId()));
        if (!cinderDisks.isEmpty()) {
            CinderDisksValidator cinderDisksValidator = getCinderDisksValidator(cinderDisks);
            return validate(cinderDisksValidator.validateCinderDiskSnapshotsLimits());
        }
        return true;
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

    protected MemoryImageBuilder getMemoryImageBuilder() {
        if (memoryBuilder == null) {
            memoryBuilder = createMemoryImageBuilder();
        }
        return memoryBuilder;
    }

    private CinderDisksValidator getCinderDisksValidator(List<CinderDisk> cinderDisks) {
        return new CinderDisksValidator(cinderDisks);
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

        Set<Guid> specifiedDiskIds = getParameters().getDiskIds();
        if (specifiedDiskIds != null && !specifiedDiskIds.isEmpty()) {
            if (!isSpecifiedDisksExist(specifiedDiskIds)) {
                return false;
            }

            List<Disk> allDisksForVm = diskDao.getAllForVm(getVm().getId());
            String notAllowSnapshot = allDisksForVm.stream()
                    .filter(disk -> specifiedDiskIds.contains(disk.getId()))
                    .filter(disk -> !disk.isAllowSnapshot())
                    .map(BaseDisk::getDiskAlias)
                    .collect(Collectors.joining(", "));

            if (!notAllowSnapshot.isEmpty()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_SNAPSHOT_NOT_SUPPORTED, String
                        .format("$diskAliases %s", notAllowSnapshot));
            }

            Set<Guid> guidsOfVmDisks = allDisksForVm.stream()
                    .map(BaseDisk::getId)
                    .collect(Collectors.toSet());

            String notAttachedToVm = specifiedDiskIds.stream()
                    .filter(guid -> !guidsOfVmDisks.contains(guid))
                    .map(guid -> diskDao.get(guid))
                    .map(BaseDisk::getDiskAlias)
                    .collect(Collectors.joining(", "));

            if (!notAttachedToVm.isEmpty()) {
                String[] replacements = { ReplacementUtils.createSetVariableString("VmName", getVm().getName()),
                        ReplacementUtils.createSetVariableString("diskAliases", notAttachedToVm) };
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_ATTACHED_TO_VM, replacements);
            }
        }

        // Initialize validators.
        VmValidator vmValidator = createVmValidator();
        StoragePoolValidator spValidator = createStoragePoolValidator();
        if (!(validateVM(vmValidator) && validate(spValidator.existsAndUp())
                && validate(vmValidator.vmNotIllegal())
                && validate(vmValidator.vmNotLocked())
                && validate(snapshotsValidator.vmNotDuringSnapshot(getVmId()))
                && validate(snapshotsValidator.vmNotInPreview(getVmId()))
                && validate(vmValidator.vmNotDuringMigration())
                && validate(vmValidator.vmNotRunningStateless())
                && (!getParameters().isSaveMemory() || validate(vmValidator.vmNotHavingPciPassthroughDevices()))
                && (!getParameters().isSaveMemory() || validate(vmValidator.vmNotHavingScsiPassthroughDevices()))
                && (!getParameters().isSaveMemory() || validate(vmValidator.vmNotHavingNvdimmDevices()))
                && (!getParameters().isSaveMemory() || validate(vmValidator.vmNotHavingTpm()))
                && (!getParameters().isSaveMemory() || validate(vmValidator.vmNotUsingMdevTypeHook())))) {
            return false;
        }

        List<DiskImage> disksList = getDisksListForChecks();
        if (disksList.size() > 0) {
            DiskImagesValidator diskImagesValidator = createDiskImageValidator(disksList);
            if (!(validate(diskImagesValidator.diskImagesNotLocked())
                    && validate(diskImagesValidator.diskImagesNotIllegal())
                    && validate(vmValidator.vmWithoutLocalDiskUserProperty())
                    && validate(diskImagesValidator.snapshotAlreadyExists(getParameters().getDiskImagesMap())))) {
                return false;
            }
        }

        return validateStorage();
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

    private boolean validateVM(VmValidator vmValidator) {
        return validate(vmValidator.vmNotSavingRestoring()) &&
                validate(vmValidator.validateVmStatusUsingMatrix(ActionType.CreateSnapshotForVm));
    }

    protected DiskImagesValidator createDiskImageValidator(List<DiskImage> disksList) {
        return new DiskImagesValidator(disksList);
    }

    protected VmValidator createVmValidator() {
        return new VmValidator(getVm());
    }

    protected List<DiskImage> getDiskImagesForVm() {
        List<Disk> disks = diskDao.getAllForVm(getVmId());
        List<DiskImage> allDisks = new ArrayList<>(getDiskImages(disks));
        allDisks.addAll(imagesHandler.getCinderLeafImages(disks));
        allDisks.addAll(imagesHandler.getManagedBlockStorageSnapshots(disks));
        return allDisks;
    }

    protected DiskExistenceValidator createDiskExistenceValidator(Set<Guid> disksGuids) {
        return Injector.injectMembers(new DiskExistenceValidator(disksGuids));
    }

    protected StoragePoolValidator createStoragePoolValidator() {
        return new StoragePoolValidator(getStoragePool());
    }

    protected MultipleStorageDomainsValidator createMultipleStorageDomainsValidator(Collection<DiskImage> disksList) {
        return new MultipleStorageDomainsValidator(getVm().getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(disksList));
    }

    protected boolean isLiveSnapshotApplicable() {
        return getParameters().getParentCommand() != ActionType.RunVm && getVm() != null
                && (getVm().isRunning() || getVm().getStatus() == VMStatus.Paused) && getVm().getRunOnVds() != null;
    }

    private boolean performLiveSnapshot(final Snapshot snapshot) {
        ActionReturnValue actionReturnValue = runInternalAction(ActionType.CreateLiveSnapshotForVm,
                createLiveSnapshotParameters(snapshot),
                ExecutionHandler.createDefaultContextForTasks(getContext()));
        if (actionReturnValue.getSucceeded()) {
            setSucceeded(true);
            return true;
        } else {
            log.error("Failed to start live snapshot on VDS");
            return false;
        }
    }

    private CreateSnapshotForVmParameters createLiveSnapshotParameters(final Snapshot snapshot) {
        CreateSnapshotForVmParameters params =  new CreateSnapshotForVmParameters();
        params.setVmId(getParameters().getVmId());
        params.setSnapshot(snapshot);
        params.setSaveMemory(getParameters().isSaveMemory());
        params.setVdsRunningOn(getVds().getId());
        params.setCreatedSnapshotId(getParameters().getCreatedSnapshotId());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        params.setShouldFreezeOrThaw(shouldFreezeOrThaw);
        if (!FeatureSupported.isAsyncLiveSnapshotSupported(getVm().getClusterCompatibilityVersion(), getVds())) {
            params.setLegacyFlow(true);
        }
        params.setCachedSelectedActiveDisks(cachedSelectedActiveDisks);
        params.setMemorySnapshotSupported(isMemorySnapshotSupported);
        params.setParentLiveMigrateDisk(getParameters().getParentCommand() == ActionType.LiveMigrateDisk);
        return params;
    }

    @Override
    protected List<ActionParametersBase> getParametersForChildCommand() {
        List<ActionParametersBase> sortedList = getParameters().getImagesParameters();
        sortedList.sort((o1, o2) -> {
            if (o1 instanceof ImagesActionsParametersBase && o2 instanceof ImagesActionsParametersBase) {
                return ((ImagesActionsParametersBase) o1).getDestinationImageId()
                        .compareTo(((ImagesActionsParametersBase) o2).getDestinationImageId());
            }
            return 0;
        });

        return sortedList;
    }

    private boolean isSnapshotCreated() {
        return getParameters().getCreatedSnapshotId() != null &&
                getParameters().getCreateSnapshotStage() == CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_VOLUME_FINISHED ||
                getParameters().getCreateSnapshotStage() == CreateSnapshotForVmParameters.CreateSnapshotStage.CREATE_SNAPSHOT_COMPLETED;
    }

    @Override
    protected void endVmCommand() {
        if (getParameters().getTaskGroupSuccess()) {
            snapshotDao.updateStatus(getParameters().getCreatedSnapshotId(), Snapshot.SnapshotStatus.OK);
        } else {
            if (!isSnapshotCreated()) {
                log.warn("No snapshot was created for VM '{}' which is in LOCKED status", getVmId());
            } else {
                Snapshot createdSnapshot = snapshotDao.get(getParameters().getCreatedSnapshotId());
                revertToActiveSnapshot(createdSnapshot.getId());
                // If the removed snapshot contained memory, remove the memory volumes
                // Note that the memory volumes might not have been created
                if (snapshotWithMemory(createdSnapshot)) {
                    removeMemoryVolumesOfSnapshot(createdSnapshot);
                }

            }
        }

        // In case of failure the memory disks will remain on the storage but not on the engine
        // this will be handled in: https://bugzilla.redhat.com/1568887
        incrementVmGeneration();
        TransactionSupport.executeInScope(TransactionScopeOption.Suppress, () -> {
            endActionOnDisks();

            return null;
        });
        setSucceeded(getParameters().getTaskGroupSuccess() &&
                (!getParameters().isLiveSnapshotRequired() || getParameters().isLiveSnapshotSucceeded()));
        getReturnValue().setEndActionTryAgain(false);
    }

    /**
     * Return the given snapshot ID's snapshot to be the active snapshot. The snapshot with the given ID is removed
     * in the process.
     *
     * @param createdSnapshotId The snapshot ID to return to being active.
     */
    private void revertToActiveSnapshot(Guid createdSnapshotId) {
        if (createdSnapshotId != null) {
            snapshotDao.remove(createdSnapshotId);
            snapshotDao.updateId(snapshotDao.getId(getVmId(), Snapshot.SnapshotType.ACTIVE), createdSnapshotId);
        }
        setSucceeded(false);
    }

    /**
     * Filter all allowed snapshot disks.
     *
     * @return list of disks to be snapshot.
     */
    protected List<DiskImage> getDisksList() {
        if (cachedSelectedActiveDisks == null) {
            List<DiskImage> imagesAndCinderForVm = getDiskImagesForVm();

            // Get disks from the specified parameters or according to the VM
            if (getParameters().getDiskIds() == null) {
                cachedSelectedActiveDisks = imagesAndCinderForVm;
            } else {
                // Get selected images from 'DiskImagesForVm' to ensure disks entities integrity
                // (i.e. only images' IDs and Cinders' IDs are relevant).
                cachedSelectedActiveDisks = getDiskImagesForVm().stream()
                        .filter(d -> getParameters().getDiskIds().contains(d.getId()))
                        .collect(Collectors.toList());
            }
        }
        return cachedSelectedActiveDisks;
    }

    private void incrementVmGeneration() {
        vmStaticDao.incrementDbGeneration(getVm().getId());
    }

    private void removeMemoryVolumesOfSnapshot(Snapshot snapshot) {
        ActionReturnValue retVal = runInternalAction(
                ActionType.RemoveMemoryVolumes,
                new RemoveMemoryVolumesParameters(snapshot, getVmId()), cloneContextAndDetachFromParent());

        if (!retVal.getSucceeded()) {
            log.error("Failed to remove memory volumes of snapshot '{}' ({})",
                    snapshot.getDescription(), snapshot.getId());
        }
    }


    private void logMemorySavingFailed() {
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        auditLogDirector.log(this, AuditLogType.USER_CREATE_LIVE_SNAPSHOT_NO_MEMORY_FAILURE);
    }

    private boolean shouldPerformLiveSnapshot(Snapshot snapshot) {
        return isLiveSnapshotApplicable && snapshot != null &&
                (snapshotWithMemory(snapshot) || hasDisksForLiveSnapshot());
    }

    private boolean hasDisksForLiveSnapshot() {
        return getDisksList()
                .stream()
                .anyMatch(diskImage -> diskImage.getDiskStorageType() == DiskStorageType.IMAGE);
    }

    private boolean snapshotWithMemory(Snapshot snapshot) {
        return getParameters().isSaveMemory() && snapshot.containsMemory();
    }

    private ActionReturnValue createSnapshotsForDisks() {
        CreateSnapshotDiskParameters parameters = new CreateSnapshotDiskParameters();
        parameters.setDiskIdsToIgnoreInChecks(getParameters().getDiskIdsToIgnoreInChecks());
        parameters.setDiskImagesMap(getParameters().getDiskImagesMap());
        parameters.setNewActiveSnapshotId(newActiveSnapshotId);
        parameters.setSnapshotType(getParameters().getSnapshotType());
        parameters.setDiskIds(getParameters().getDiskIds());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.PARENT_MANAGED);
        parameters.setVmId(getVmId());
        parameters.setSessionId(getParameters().getSessionId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setLiveSnapshot(isLiveSnapshotApplicable() && hasDisksForLiveSnapshot());
        return runInternalAction(ActionType.CreateSnapshotDisk,
                parameters,
                ExecutionHandler.createDefaultContextForTasks(getContext(), getLock()));
    }

    private void fastForwardDisksToActiveSnapshot() {
        if (getParameters().getDiskIds() != null) {
            // Fast-forward non-included disks to active snapshot
            getDiskImagesForVm().stream()
                    // Remove disks included in snapshot
                    .filter(d -> !getParameters().getDiskIds().contains(d.getId()))
                    .forEach(d -> imageDao.updateImageVmSnapshotId(
                            d.getDiskStorageType() == DiskStorageType.IMAGE ? d.getImageId() : d.getId(),
                            newActiveSnapshotId));
        }
    }

    private void addSnapshotToDB(Guid snapshotId, MemoryImageBuilder memoryImageBuilder) {
        // Reset cachedSelectedActiveDisks so new Cinder volumes can be fetched when calling getDisksList.
        cachedSelectedActiveDisks = null;
        getSnapshotsManager().addSnapshot(snapshotId,
                getParameters().getDescription(),
                Snapshot.SnapshotStatus.LOCKED,
                getParameters().getSnapshotType(),
                getVm(),
                true,
                null,
                memoryImageBuilder.getMemoryDiskId(),
                memoryImageBuilder.getMetadataDiskId(),
                null,
                getDisksList(),
                null,
                getCompensationContext());
    }

    /**
     * Freezing the VM is needed for live snapshot with Cinder disks or when forced selected.
     */
    private void freezeVm() {
        if (!shouldFreezeOrThaw) {
            return;
        }

        VDSReturnValue returnValue = null;
        boolean allowInconsistent = Config.<Boolean>getValue(ConfigValues.LiveSnapshotAllowInconsistent);
        try {
            auditLogDirector.log(this, AuditLogType.FREEZE_VM_INITIATED);
            returnValue = runVdsCommand(VDSCommandType.Freeze, new VdsAndVmIDVDSParametersBase(
                    getVds().getId(), getVmId()));
        } catch (EngineException e) {
            if (allowInconsistent) {
                handleInconsistent();
            } else {
                handleFreezeVmFailure(e);
            }
            return;
        }
        if (returnValue != null && returnValue.getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.FREEZE_VM_SUCCESS);
        } else {
            if (allowInconsistent) {
                handleInconsistent();
            } else {
                handleFreezeVmFailure(new EngineException(EngineError.freezeErr));
            }
        }
    }

    private void handleFreezeVmFailure(EngineException e) {
        handleVmFailure(e, AuditLogType.FAILED_TO_FREEZE_VM,
                "Could not freeze VM guest filesystems due to an error: {}");
        throw new EngineException(EngineError.freezeErr);
    }

    private void handleInconsistent() {
        log.warn("Could not freeze VM guest filesystems, proceeding with an inconsistent snapshot.");
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        auditLogDirector.log(this, AuditLogType.FAILED_TO_FREEZE_VM);
    }


    private void handleVmFailure(EngineException e, AuditLogType auditLogType, String warnMessage) {
        log.warn(warnMessage, e.getMessage());
        log.debug("Exception", e);
        addCustomValue("SnapshotName", getSnapshotName());
        addCustomValue("VmName", getVmName());
        updateCallStackFromThrowable(e);
        auditLogDirector.log(this, auditLogType);
    }

    // LiveSnapshotPerformFreezeInEngine value will perform the freeze/thaw command in the engine in order to avoid
    // possible data corruption. This is relevant only for live snapshot without memory.
    // In case the freeze is stuck in VDSM and there is no reliable way to tell if the volume is used or not.
    private boolean shouldFreezeOrThawVm() {
        if (!isLiveSnapshotApplicable) {
            return false; // only relevant for live snapshots
        }

        if (isMemorySnapshotSupported && getParameters().isSaveMemory()) {
            return false; // irrelevant for snapshots that contain memory
        }

        if (getParameters().getParentCommand() == ActionType.LiveMigrateDisk) {
            return false; // irrelevant for snapshot taken as part of live storage migration
        }

        if (FeatureSupported.isFreezeInEngineEnabled(getVm().getClusterCompatibilityVersion())) {
            return true;
        }

        if (diskOfTypeExists(DiskStorageType.CINDER) || diskOfTypeExists(DiskStorageType.MANAGED_BLOCK_STORAGE)) {
            return true;
        }
        return false;
    }

    private boolean diskOfTypeExists(DiskStorageType type) {
        return getDisksList()
                .stream()
                .anyMatch(disk -> type.equals(disk.getDiskStorageType()));
    }

    private MemoryImageBuilder createMemoryImageBuilder() {
        if (!isMemorySnapshotSupported) {
            return new NullableMemoryImageBuilder();
        }

        if (getParameters().getSnapshotType() == Snapshot.SnapshotType.STATELESS) {
            return new StatelessSnapshotMemoryImageBuilder(getVm());
        }

        if (getParameters().isSaveMemory() && isLiveSnapshotApplicable) {
            boolean wipeAfterDelete = getDisksList().stream().anyMatch(DiskImage::isWipeAfterDelete);
            return new LiveSnapshotMemoryImageBuilder(getVm(), cachedStorageDomainId,
                    this, vmOverheadCalculator, getParameters().getDescription(), wipeAfterDelete);
        }

        return new NullableMemoryImageBuilder();
    }

    /**
     * Check if Memory Snapshot is supported
     */
    private boolean isMemorySnapshotSupported() {
        return FeatureSupported.isMemorySnapshotSupportedByArchitecture(
                getVm().getClusterArch(), getVm().getCompatibilityVersion());
    }

    private Guid updateActiveSnapshotId() {
        final Snapshot activeSnapshot = snapshotDao.get(getVmId(), Snapshot.SnapshotType.ACTIVE);
        final Guid activeSnapshotId = activeSnapshot.getId();

        TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
            getCompensationContext().snapshotEntity(activeSnapshot);
            snapshotDao.updateId(activeSnapshotId, newActiveSnapshotId);
            activeSnapshot.setId(newActiveSnapshotId);
            getCompensationContext().snapshotNewEntity(activeSnapshot);
            getCompensationContext().stateChanged();
            return null;
        });

        return activeSnapshotId;
    }

    private boolean validateStorage() {
        List<DiskImage> vmDisksList = getDisksListForChecks();
        vmDisksList = ImagesHandler.getDisksDummiesForStorageAllocations(vmDisksList);
        List<DiskImage> allDisks = new ArrayList<>(vmDisksList);

        MemoryDisks memoryDisks = null;
        if (getParameters().isSaveMemory()) {
            memoryDisks = MemoryUtils.createDiskDummies(vmOverheadCalculator.getSnapshotMemorySizeInBytes(getVm()),
                    MemoryUtils.METADATA_SIZE_IN_BYTES);
            if (Guid.Empty.equals(getStorageDomainIdForVmMemory(memoryDisks))) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
            }
            allDisks.addAll(memoryDisks.asList());
        }

        MultipleStorageDomainsValidator sdValidator = createMultipleStorageDomainsValidator(allDisks);
        if (!validate(sdValidator.allDomainsExistAndActive())
                || !validate(sdValidator.allDomainsWithinThresholds())
                || !validateCinder()
                || !validate(sdValidator.isSupportedByManagedBlockStorageDomains(getActionType()))) {
            return false;
        }

        if (memoryDisks == null) { //no memory volumes
            return validate(sdValidator.allDomainsHaveSpaceForNewDisks(vmDisksList));
        }

        return validate(sdValidator.allDomainsHaveSpaceForAllDisks(vmDisksList, memoryDisks.asList()));
    }

    private List<DiskImage> getDiskImages(List<Disk> disks) {
        if (cachedImagesDisks == null) {
            cachedImagesDisks = DisksFilter.filterImageDisks(disks, ONLY_NOT_SHAREABLE,
                    ONLY_SNAPABLE, ONLY_ACTIVE);
        }

        return cachedImagesDisks;
    }

    private boolean isSpecifiedDisksExist(Set<Guid> disks) {
        DiskExistenceValidator diskExistenceValidator = createDiskExistenceValidator(disks);
        if (!validate(diskExistenceValidator.disksNotExist())) {
            return false;
        }

        return true;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
        addValidationMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
    }

    @Override
    protected ActionType getChildActionType() {
        return ActionType.CreateSnapshotDisk;
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
            cachedSnapshotIsBeingTakenMessage =
                    new LockMessage(EngineMessage.ACTION_TYPE_FAILED_SNAPSHOT_IS_BEING_TAKEN_FOR_VM)
                            .withOptional("VmName", getVmName())
                            .toString();
        }
        return cachedSnapshotIsBeingTakenMessage;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    protected boolean overrideChildCommandSuccess() {
        // If this command fails we would want the children to execute their endWithFailure flow
        return true;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
