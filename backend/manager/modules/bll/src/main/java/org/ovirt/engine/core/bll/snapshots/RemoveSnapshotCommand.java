package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Merges snapshots either live or non-live based on VM status
 */
@DisableInPrepareMode
public class RemoveSnapshotCommand<T extends RemoveSnapshotParameters> extends VmCommand<T>
        implements QuotaStorageDependent {
    private List<DiskImage> _sourceImages = null;

    public RemoveSnapshotCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    public RemoveSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private void initializeObjectState() {
        if (StringUtils.isEmpty(getSnapshotName())) {
            Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());
            if (snapshot != null) {
                setSnapshotName(snapshot.getDescription());
                getParameters().setUseCinderCommandCallback(
                        !ImagesHandler.filterDisksBasedOnCinder(getSourceImages()).isEmpty());
            }
        }
        setStoragePoolId(getVm().getStoragePoolId());
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            initializeObjectState();
            jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), getSnapshotName());
        }
        return jobProperties;
    }

    /**
     * @return The image snapshots associated with the VM snapshot.
     * Note that the first time this method is run it issues Dao call.
     */
    protected List<DiskImage> getSourceImages() {
        if (_sourceImages == null) {
            _sourceImages = getDiskImageDao().getAllSnapshotsForVmSnapshot(getParameters().getSnapshotId());
        }
        return _sourceImages;
    }

    @Override
    protected void executeCommand() {
        if (!getVm().isDown() && !getVm().isQualifiedForSnapshotMerge()) {
            log.error("Cannot remove VM snapshot. Vm is not Down, Up or Paused");
            throw new EngineException(EngineError.VM_NOT_QUALIFIED_FOR_SNAPSHOT_MERGE);
        }

        final Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());

        boolean snapshotHasImages = hasImages();
        boolean removeSnapshotMemory = isMemoryVolumeRemoveable(snapshot.getMemoryVolume());

        // If the VM hasn't got any images and memory - simply remove the snapshot.
        // No need for locking, VDSM tasks, and all that jazz.
        if (!snapshotHasImages && !removeSnapshotMemory) {
            getSnapshotDao().remove(getParameters().getSnapshotId());
            setSucceeded(true);
            return;
        }

        lockSnapshot(snapshot);
        freeLock();
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));

        boolean useTaskManagerToRemoveMemory = false;
        if (snapshotHasImages) {
            removeImages();

            if (getSnapshotActionType() == VdcActionType.RemoveSnapshotSingleDiskLive) {
                persistCommand(getParameters().getParentCommand(), true);
                useTaskManagerToRemoveMemory = true;
            }
        }

        if (removeSnapshotMemory) {
            removeMemory(snapshot, useTaskManagerToRemoveMemory);
            if (!snapshotHasImages) {
                // no async tasks - ending command manually
                endVmCommand();
            }
        }

        setSucceeded(true);
    }

    /**
     * There is a one to many relation between memory volumes and snapshots, so memory
     * volumes should be removed only if the only snapshot that points to them is removed
     */
    protected boolean isMemoryVolumeRemoveable(String memoryVolume) {
        return !memoryVolume.isEmpty() &&
                getDbFacade().getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 1;
    }

    private void removeMemory(final Snapshot snapshot, boolean useTaskManager) {
        RemoveMemoryVolumesParameters parameters = new RemoveMemoryVolumesParameters(snapshot.getMemoryVolume(), getVmId());
        if (useTaskManager) {
            CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.RemoveMemoryVolumes, parameters, cloneContextAndDetachFromParent());
        } else {
            VdcReturnValueBase ret = runInternalAction(VdcActionType.RemoveMemoryVolumes, parameters);
            if (!ret.getSucceeded()) {
                log.error("Cannot remove memory volumes for snapshot '{}'", snapshot.getId());
            }
        }
    }

    private void removeImages() {
        List<CinderDisk> cinderDisks = new ArrayList<>();
        for (final DiskImage source : getSourceImages()) {
            if (source.getDiskStorageType() == DiskStorageType.CINDER) {
                cinderDisks.add((CinderDisk) source);
                continue;
            }

            // The following is ok because we have tested in the validate that the vm
            // is not a template and the vm is not in preview mode and that
            // this is not the active snapshot.
            List<DiskImage> images = getDiskImageDao().getAllSnapshotsForParent(source.getImageId());
            DiskImage dest = null;
            if (!images.isEmpty()) {
                dest = images.get(0);
            }

            if (getSnapshotActionType() == VdcActionType.RemoveSnapshotSingleDisk) {
                VdcReturnValueBase vdcReturnValue = runInternalActionWithTasksContext(
                        getSnapshotActionType(),
                        buildRemoveSnapshotSingleDiskParameters(source, dest));
                if (vdcReturnValue != null && vdcReturnValue.getInternalVdsmTaskIdList() != null) {
                    getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
                }
            } else {
                CommandCoordinatorUtil.executeAsyncCommand(
                        getSnapshotActionType(),
                        buildRemoveSnapshotSingleDiskParameters(source, dest),
                        cloneContextAndDetachFromParent());
            }

            List<Guid> quotasToRemoveFromCache = new ArrayList<>();
            quotasToRemoveFromCache.add(source.getQuotaId());
            if (dest != null) {
                quotasToRemoveFromCache.add(dest.getQuotaId());
            }
            getQuotaManager().removeQuotaFromCache(getStoragePoolId(), quotasToRemoveFromCache);
        }
        if (!cinderDisks.isEmpty()) {
            handleCinderSnapshotDisks(cinderDisks);
        }
    }

    private void handleCinderSnapshotDisks(List<CinderDisk> cinderDisks) {
        for (CinderDisk cinderDisk : cinderDisks) {
            VdcReturnValueBase vdcReturnValueBase = runInternalAction(
                    VdcActionType.RemoveCinderSnapshotDisk,
                    buildRemoveCinderSnapshotDiskParameters(cinderDisk),
                    cloneContextAndDetachFromParent());
            if (!vdcReturnValueBase.getSucceeded()) {
                log.error("Error removing snapshots for Cinder disk");
            }
        }
    }

    private void lockSnapshot(final Snapshot snapshot) {
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntityStatus(snapshot);
            getSnapshotDao().updateStatus(
                    getParameters().getSnapshotId(), SnapshotStatus.LOCKED);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    private RemoveSnapshotSingleDiskParameters buildRemoveSnapshotSingleDiskParameters(final DiskImage source,
            DiskImage dest) {
        RemoveSnapshotSingleDiskParameters parameters =
                new RemoveSnapshotSingleDiskParameters(source.getImageId(), getVmId());
        parameters.setDestinationImageId(dest != null ? dest.getImageId() : null);
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setParentParameters(getParameters());
        parameters.setParentCommand(getActionType());
        parameters.setCommandType(getSnapshotActionType());
        parameters.setVdsId(getVm().getRunOnVds());
        parameters.setEndProcedure(getVm().isQualifiedForLiveSnapshotMerge() ? EndProcedure.COMMAND_MANAGED :
                EndProcedure.PARENT_MANAGED);
        return parameters;
    }

    private ImagesContainterParametersBase buildRemoveCinderSnapshotDiskParameters(CinderDisk cinderDisk) {
        ImagesContainterParametersBase removeCinderSnapshotParams =
                new ImagesContainterParametersBase(cinderDisk.getImageId());
        removeCinderSnapshotParams.setDestinationImageId(cinderDisk.getImageId());
        removeCinderSnapshotParams.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        removeCinderSnapshotParams.setParentCommand(getActionType());
        removeCinderSnapshotParams.setParentParameters(getParameters());
        return removeCinderSnapshotParams;
    }

    @Override
    protected void endVmCommand() {
        initializeObjectState();
        if (getParameters().getTaskGroupSuccess()) {
            getSnapshotDao().remove(getParameters().getSnapshotId());
        } else {
            List<String> failedToRemoveDisks = new ArrayList<>();
            Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());

            for (VdcActionParametersBase parameters : getParameters().getImagesParameters()) {
                ImagesContainterParametersBase imagesParams = parameters instanceof ImagesContainterParametersBase ?
                        (ImagesContainterParametersBase) parameters : null;

                if (imagesParams == null) {
                    // Shouldn't happen as for now ImagesParameters list contains only
                    // instances of ImagesContainterParametersBase objects.
                    continue;
                }

                if (imagesParams.getTaskGroupSuccess()) {
                    snapshot = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(
                            snapshot, imagesParams.getImageId());
                } else {
                    log.error("Could not delete image '{}' from snapshot '{}'",
                            imagesParams.getImageId(), getParameters().getSnapshotId());

                    DiskImage diskImage = getDiskImageDao().getSnapshotById(imagesParams.getImageId());
                    failedToRemoveDisks.add(diskImage.getDiskAlias());
                }
            }

            // Remove memory volume and update the dao.
            // Note: on failure, we can treat memory volume deletion as deleting an image
            // and remove it from the snapshot entity (rollback isn't applicable).
            snapshot.setMemoryVolume("");
            getSnapshotDao().update(snapshot);

            if (!failedToRemoveDisks.isEmpty()) {
                addCustomValue("DiskAliases", StringUtils.join(failedToRemoveDisks, ", "));
                auditLogDirector.log(this, AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE_PARTIAL_SNAPSHOT);
            }

            getSnapshotDao().updateStatus(getParameters().getSnapshotId(), SnapshotStatus.OK);
        }

        super.endVmCommand();
    }

    /**
     * @return Don't override the child success, we want merged image chains to be so also in the DB, or else we will be
     *         out of sync with the storage and this is not a good situation.
     */
    @Override
    protected boolean overrideChildCommandSuccess() {
        return false;
    }

    @Override
    protected boolean validate() {
        initializeObjectState();

        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        VmValidator vmValidator = createVmValidator(getVm());
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp()) ||
                !validateVmNotDuringSnapshot() ||
                !validateVmNotInPreview() ||
                !validateSnapshotExists() ||
                !validateSnapshotType() ||
                !validate(vmValidator.vmQualifiedForSnapshotMerge()) ||
                !validate(vmValidator.vmNotHavingDeviceSnapshotsAttachedToOtherVms(false))) {
            return false;
        }

        if (hasImages()) {
            // Check the VM's images
            if (!validateImages()) {
                return false;
            }

            // check that we are not deleting the template
            if (!validateImageNotInTemplate()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_IMAGE_TEMPLATE);
            }

            if (!validateStorageDomains()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the storage domains.
     *
     * Each domain is validated for status, threshold and for enough free space to perform removeSnapshot.
     * The remove snapshot logic in VDSM includes creating a new temporary volume which might be as large as the disk's
     * actual size.
     * Hence, as part of the validation, we sum up all the disks virtual sizes, for each storage domain.
     *
     * @return True if there is enough space in all relevant storage domains. False otherwise.
     */
    protected boolean validateStorageDomains() {
        List<DiskImage> disksList = getDisksListForStorageAllocations();
        MultipleStorageDomainsValidator storageDomainsValidator = getStorageDomainsValidator(getStoragePoolId(), getStorageDomainsIds());
        return validate(storageDomainsValidator.allDomainsExistAndActive())
                && validate(storageDomainsValidator.allDomainsWithinThresholds())
                && validate(storageDomainsValidator.allDomainsHaveSpaceForClonedDisks(disksList));
    }

    protected List<DiskImage> getDisksListForStorageAllocations() {
        if (getSnapshotActionType() == VdcActionType.RemoveSnapshotSingleDiskLive) {
            return ImagesHandler.getDisksDummiesForStorageAllocations(getSourceImages());
        }
        return ImagesHandler.getSnapshotsDummiesForStorageAllocations(getSourceImages());
    }

    protected Collection<Guid> getStorageDomainsIds() {
        return ImagesHandler.getAllStorageIdsForImageIds(getSourceImages());
    }

    protected MultipleStorageDomainsValidator getStorageDomainsValidator(Guid spId, Collection<Guid> sdIds) {
        return new MultipleStorageDomainsValidator(spId, sdIds);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    protected boolean validateVmNotDuringSnapshot() {
        return validate(createSnapshotValidator().vmNotDuringSnapshot(getVmId()));
    }

    protected boolean validateVmNotInPreview() {
        return validate(createSnapshotValidator().vmNotInPreview(getVmId()));
    }

    protected boolean validateSnapshotExists() {
        return validate(createSnapshotValidator().snapshotExists(getVmId(), getParameters().getSnapshotId()));
    }

    protected boolean validateSnapshotType() {
        Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());
        return validate(createSnapshotValidator().isRegularSnapshot(snapshot));
    }

    protected boolean validateImages() {
        List<DiskImage> imagesToValidate = getDiskImagesToValidate();

        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(imagesToValidate);

        return validateImagesNotLocked(diskImagesValidator) &&
                (getVm().isQualifiedForLiveSnapshotMerge() || validate(diskImagesValidator.diskImagesNotIllegal()));
    }

    private boolean validateImagesNotLocked(DiskImagesValidator diskImagesValidator) {
        return !getParameters().isNeedsLocking() || validate(diskImagesValidator.diskImagesNotLocked());
    }

    private List<DiskImage> getDiskImagesToValidate() {
        List<Disk> disks = getDiskDao().getAllForVm(getVmId());
        List<DiskImage> allDisks = ImagesHandler.filterImageDisks(disks, true, false, true);
        List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(disks);
        allDisks.addAll(cinderDisks);
        return allDisks;
    }

    protected boolean validateImageNotInTemplate() {
        return getVmTemplateDao().get(getRepresentativeSourceImageId()) == null;
    }

    private boolean hasImages() {
        return !getSourceImages().isEmpty();
    }

    private Guid getRepresentativeSourceImageId() {
        return getSourceImages().get(0).getImageId();
    }

    protected SnapshotsValidator createSnapshotValidator() {
        return new SnapshotsValidator();
    }

    protected VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_REMOVE_SNAPSHOT : AuditLogType.USER_FAILED_REMOVE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    private VdcActionType getSnapshotActionType() {
        return getVm().isQualifiedForLiveSnapshotMerge() ? VdcActionType.RemoveSnapshotSingleDiskLive : VdcActionType.RemoveSnapshotSingleDisk;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getParameters().isNeedsLocking() ?
                Collections.singletonMap(getVmId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED))
                : null;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        //return empty list - the command only release quota so it could never fail the quota check
        return new ArrayList<>();
    }

    @Override
    public CommandCallback getCallback() {
        if (getVm().isQualifiedForLiveSnapshotMerge() || getParameters().isUseCinderCommandCallback()) {
            return new ConcurrentChildCommandsExecutionCallback();
        }
        return null;
    }
}
