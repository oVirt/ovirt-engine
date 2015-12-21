package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.CINDERStorageHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.RestoreAllCinderSnapshotsParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

/**
 * Restores the given snapshot, including all the VM configuration that was stored in it.<br>
 * Any obsolete snapshots will be deleted:<br>
 * * If the restore is done to the {@link SnapshotType#STATELESS} snapshot then the stateless snapshot data is restored
 * into the active snapshot, and the "old" active snapshot is deleted & replaced by the stateless snapshot.<br>
 * * If the restore is done to a branch of a snapshot which is {@link SnapshotStatus#IN_PREVIEW}, then the other branch
 * will be deleted (ie if the {@link SnapshotType#ACTIVE} snapshot is kept, then the branch of
 * {@link SnapshotType#PREVIEW} is deleted up to the previewed snapshot, otherwise the active one is deleted).<br>
 * <br>
 * <b>Note:</b> It is <b>NOT POSSIBLE</b> to restore to a snapshot of any other type other than those stated above,
 * since this command can only handle the aforementioned cases.
 */
public class RestoreAllSnapshotsCommand<T extends RestoreAllSnapshotsParameters> extends VmCommand<T> implements QuotaStorageDependent {

    private final Set<Guid> snapshotsToRemove = new HashSet<>();
    private Snapshot snapshot;
    List<DiskImage> imagesToRestore = new ArrayList<>();
    List<DiskImage> imagesFromPreviewSnapshot = new ArrayList<>();

    /**
     * The snapshot id which will be removed (the stateless/preview/active image).
     */
    private Guid removedSnapshotId;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RestoreAllSnapshotsCommand(Guid commandId) {
        super(commandId);
    }

    public RestoreAllSnapshotsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
    }


    public RestoreAllSnapshotsCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeVmCommand() {

        if (!getImagesList().isEmpty()) {
            lockVmWithCompensationIfNeeded();
            if (!isInternalExecution()) {
                freeLock();
            }
        }

        restoreSnapshotAndRemoveObsoleteSnapshots(getSnapshot());

        boolean succeeded = true;
        List<CinderDisk> cinderDisksToRestore = new ArrayList<>();
        for (DiskImage image : imagesToRestore) {
            if (image.getImageStatus() != ImageStatus.ILLEGAL) {
                if (image.getDiskStorageType() == DiskStorageType.CINDER) {
                    cinderDisksToRestore.add((CinderDisk) image);
                    continue;
                }
                ImagesContainterParametersBase params = new RestoreFromSnapshotParameters(image.getImageId(),
                        getVmId(), getSnapshot(), removedSnapshotId);
                VdcReturnValueBase returnValue = runAsyncTask(VdcActionType.RestoreFromSnapshot, params);
                // Save the first fault
                if (succeeded && !returnValue.getSucceeded()) {
                    succeeded = false;
                    getReturnValue().setFault(returnValue.getFault());
                }
            }
        }

        List<CinderDisk> cinderVolumesToRemove = new ArrayList<>();
        List<CinderDisk> cinderDisksToRemove = new ArrayList<>();
        removeSnapshotsFromDB();
        removeUnusedImages(cinderVolumesToRemove);

        if (!getTaskIdList().isEmpty() || !cinderDisksToRestore.isEmpty() || !cinderVolumesToRemove.isEmpty()) {
            deleteOrphanedImages(cinderDisksToRemove);
            if (!restoreCinderDisks(removedSnapshotId, cinderDisksToRestore, cinderDisksToRemove, cinderVolumesToRemove)) {
                log.error("Error to restore Cinder volumes snapshots");
            }
        } else {
            getVmStaticDao().incrementDbGeneration(getVm().getId());
            getSnapshotDao().updateStatus(getSnapshot().getId(), SnapshotStatus.OK);
            unlockVm();
        }

        setSucceeded(succeeded);
    }

    protected boolean restoreAllCinderDisks(List<CinderDisk> cinderDisksToRestore,
                                            List<CinderDisk> cinderDisksToRemove,
                                            List<CinderDisk> cinderVolumesToRemove,
                                            Guid removedSnapshotId) {
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RestoreAllCinderSnapshots,
                        buildCinderChildCommandParameters(cinderDisksToRestore,
                                cinderDisksToRemove,
                                cinderVolumesToRemove,
                                removedSnapshotId),
                cloneContextAndDetachFromParent(),
                CINDERStorageHelper.getStorageEntities(cinderDisksToRestore));
        try {
            VdcReturnValueBase vdcReturnValueBase = future.get();
            if (!vdcReturnValueBase.getSucceeded()) {
                getReturnValue().setFault(vdcReturnValueBase.getFault());
                log.error("Error while restoring Cinder snapshot");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error deleting Cinder volumes for restore snapshot", e);
            return false;
        }
        return true;
    }

    /**
     * Returns the initial Cinder volume to delete all the Cinder volumes from. The restore process of the Cinder volume
     * should use this volume to get all its descendants and remove them all. The initial volume is chosen by taking the
     * previewed snapshot, fetch the parent volume, and retun the other volume.
     *
     * @param cinderVolume
     *            - The cinder volume we fetch the initial volume from (Most of the time it will be the active volume) -
     * @return - The initial volume of the Cinder disk to delete from.
     */
    private CinderDisk getInitialCinderVolumeToDelete(DiskImage cinderVolume) {
        CinderDisk initialVolumeToDelete = null;
        List<DiskImage> snapshotsForParent = getDiskImageDao().getAllSnapshotsForParent(cinderVolume.getParentId());
        for (DiskImage snapshot : snapshotsForParent) {
            if (!snapshot.getImageId().equals(cinderVolume.getImageId())) {
                initialVolumeToDelete = (CinderDisk) snapshot;
            }
        }
        return initialVolumeToDelete;
    }

    private RestoreAllCinderSnapshotsParameters buildCinderChildCommandParameters(List<CinderDisk> cinderDisksToRestore,
            List<CinderDisk> cinderDisksToRemove,
            List<CinderDisk> cinderVolumesToRemove,
            Guid removedSnapshotId) {
        RestoreAllCinderSnapshotsParameters restoreParams =
                new RestoreAllCinderSnapshotsParameters(getVmId(),
                        cinderDisksToRestore,
                        cinderDisksToRemove,
                        cinderVolumesToRemove);
        restoreParams.setRemovedSnapshotId(removedSnapshotId);
        restoreParams.setSnapshot(getSnapshot());
        restoreParams.setParentHasTasks(!getReturnValue().getVdsmTaskIdList().isEmpty());
        restoreParams.setParentCommand(getActionType());
        restoreParams.setParentParameters(getParameters());
        return withRootCommandInfo(restoreParams, getActionType());
    }

    private Snapshot getSnapshot() {
        if (snapshot == null) {
            switch (getParameters().getSnapshotAction()) {
            case UNDO:
                snapshot = getSnapshotDao().get(getVmId(), SnapshotType.PREVIEW);
                break;
            case COMMIT:
                snapshot = getSnapshotDao().get(getVmId(), SnapshotStatus.IN_PREVIEW);
                break;
            case RESTORE_STATELESS:
                snapshot = getSnapshotDao().get(getVmId(), SnapshotType.STATELESS);
                break;
            default:
                log.error("The Snapshot Action '{}' is not valid", getParameters().getSnapshotAction());
            }

            // We initialize the snapshotId in the parameters so we can use it in the endVmCommand
            // to unlock the snapshot, after the task that creates the snapshot finishes.
            if (snapshot != null) {
                getParameters().setSnapshotId(snapshot.getId());
            }
        }
        return snapshot;
    }

    protected void removeSnapshotsFromDB() {
        for (Guid snapshotId : snapshotsToRemove) {
            Snapshot snap = getSnapshotDao().get(snapshotId);
            // Cinder volumes might not have correlated snapshot.
            if (snap != null) {
                String memoryVolume = getSnapshotDao().get(snapshotId).getMemoryVolume();
                if (!memoryVolume.isEmpty() &&
                        getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 1) {
                    boolean succeed = removeMemoryVolumes(memoryVolume, getActionType(), false);
                    if (!succeed) {
                        log.error("Failed to remove memory '{}' of snapshot '{}'",
                                memoryVolume, snapshotId);
                    }
                }
                getSnapshotDao().remove(snapshotId);
            }
        }
    }

    protected void deleteOrphanedImages(List<CinderDisk> cinderDisksToRemove) {
        VdcReturnValueBase returnValue;
        boolean noImagesRemovedYet = getTaskIdList().isEmpty();
        Set<Guid> deletedDisksIds = new HashSet<>();
        for (DiskImage image : getDiskImageDao().getImagesWithNoDisk(getVm().getId())) {
            if (!deletedDisksIds.contains(image.getId())) {
                deletedDisksIds.add(image.getId());
                if (image.getDiskStorageType() == DiskStorageType.CINDER) {
                    cinderDisksToRemove.add((CinderDisk) image);
                    continue;
                }
                returnValue = runAsyncTask(VdcActionType.RemoveImage,
                        new RemoveImageParameters(image.getImageId()));
                if (!returnValue.getSucceeded() && noImagesRemovedYet) {
                    setSucceeded(false);
                    getReturnValue().setFault(returnValue.getFault());
                    return;
                }

                noImagesRemovedYet = false;
            }
        }
    }

    private boolean restoreCinderDisks(Guid removedSnapshotId, List<CinderDisk> cinderDisksToRestore,
                                  List<CinderDisk> cinderDisksToRemove,
                                  List<CinderDisk> cinderVolumesToRemove) {
        if (!cinderDisksToRestore.isEmpty() || !cinderDisksToRemove.isEmpty() || !cinderVolumesToRemove.isEmpty()) {
            return restoreAllCinderDisks(cinderDisksToRestore,
                    cinderDisksToRemove,
                    cinderVolumesToRemove,
                    removedSnapshotId);
        }
        return true;
    }

    private void removeUnusedImages(List<CinderDisk> cinderVolumesToRemove) {
        Set<Guid> imageIdsUsedByActiveSnapshot = new HashSet<>();
        for (DiskImage diskImage : getImagesList()) {
            imageIdsUsedByActiveSnapshot.add(diskImage.getId());
        }

        List<DiskImage> imagesToRemove = new ArrayList<>();

        for (Guid snapshotToRemove : snapshotsToRemove) {
            List<DiskImage> snapshotDiskImages = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotToRemove);
            imagesToRemove.addAll(snapshotDiskImages);
        }

        Set<Guid> removeInProcessImageIds = new HashSet<>();
        for (DiskImage diskImage : imagesToRemove) {
            if (imageIdsUsedByActiveSnapshot.contains(diskImage.getId()) ||
                    removeInProcessImageIds.contains(diskImage.getId())) {
                continue;
            }
            DiskImage previewedDiskImage = null;
            for (DiskImage diskImageFromPreview : imagesFromPreviewSnapshot) {
                if (diskImageFromPreview.getImageId().equals(diskImage.getImageId())) {
                    previewedDiskImage = diskImageFromPreview;
                    break;
                }
            }
            if (previewedDiskImage != null && (previewedDiskImage.getDiskStorageType() == DiskStorageType.CINDER)) {
                cinderVolumesToRemove.add((CinderDisk) previewedDiskImage);
                continue;
            }
            VdcReturnValueBase retValue = runAsyncTask(VdcActionType.RemoveImage,
                    new RemoveImageParameters(diskImage.getImageId()));

            if (retValue.getSucceeded()) {
                removeInProcessImageIds.add(diskImage.getImageId());
            } else {
                log.error("Failed to remove image '{}'", diskImage.getImageId());
            }
        }
    }

    /**
     * Run the given command as async task, which includes these steps:
     * <ul>
     * <li>Add parent info to task parameters.</li>
     * <li>Run with current command's {@link org.ovirt.engine.core.bll.job.ExecutionContext}.</li>
     * <li>Add son parameters to saved image parameters.</li>
     * <li>Add son task IDs to list of task IDs.</li>
     * </ul>
     *
     * @param taskType
     *            The type of the command to run as async task.
     * @param params
     *            The command parameters.
     * @return The return value from the task.
     */
    private VdcReturnValueBase runAsyncTask(VdcActionType taskType, ImagesContainterParametersBase params) {
        VdcReturnValueBase returnValue;
        params.setEntityInfo(getParameters().getEntityInfo());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setCommandType(taskType);
        returnValue = runInternalActionWithTasksContext(
                taskType,
                params);
        getTaskIdList().addAll(returnValue.getInternalVdsmTaskIdList());
        return returnValue;
    }

    /**
     * Restore the snapshot - if it is not the active snapshot, then the VM configuration will be restored.<br>
     * Additionally, remove all obsolete snapshots (The one after stateless, or the preview chain which was not chosen).
     */
    protected void restoreSnapshotAndRemoveObsoleteSnapshots(Snapshot targetSnapshot) {
        Guid activeSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        List<DiskImage> imagesFromActiveSnapshot = getDiskImageDao().getAllSnapshotsForVmSnapshot(activeSnapshotId);

        Snapshot previewedSnapshot = getSnapshotDao().get(getVmId(), SnapshotType.PREVIEW);
        if (previewedSnapshot != null) {
            SnapshotVmConfigurationHelper snapshotVmConfigurationHelper = new SnapshotVmConfigurationHelper();
            VM vmFromConf = snapshotVmConfigurationHelper.getVmFromConfiguration(
                    previewedSnapshot.getVmConfiguration(), previewedSnapshot.getVmId(), previewedSnapshot.getId());
            imagesFromPreviewSnapshot.addAll(vmFromConf.getImages());
        }
        List<DiskImage> intersection = ImagesHandler.imagesIntersection(imagesFromActiveSnapshot, imagesFromPreviewSnapshot);

        switch (targetSnapshot.getType()) {
        case PREVIEW:
            getSnapshotDao().updateStatus(
                    getSnapshotDao().getId(getVmId(), SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW),
                    SnapshotStatus.OK);

            getParameters().setImages((List<DiskImage>) CollectionUtils.union(imagesFromPreviewSnapshot, intersection));
            for (DiskImage imageFromPreviewSnapshot : imagesFromPreviewSnapshot) {
                if (imageFromPreviewSnapshot.getDiskStorageType() != DiskStorageType.CINDER) {
                    imagesToRestore.add(imageFromPreviewSnapshot);
                } else {
                    for (DiskImage diskImageFromIntersection : intersection) {
                        if (diskImageFromIntersection.getId().equals(imageFromPreviewSnapshot.getId())) {
                            imagesToRestore.add(diskImageFromIntersection);
                        }
                    }
                }
            }
            updateSnapshotIdForSkipRestoreImages(
                    ImagesHandler.imagesSubtract(imagesFromActiveSnapshot, imagesToRestore), targetSnapshot.getId());
            restoreConfiguration(targetSnapshot);
            break;

        case STATELESS:
            imagesToRestore = getParameters().getImages();
            restoreConfiguration(targetSnapshot);
            break;

        case REGULAR:
            prepareToDeletePreviewBranch(imagesFromActiveSnapshot);

            // Set the active snapshot's images as target images for restore, because they are what we keep.
            getParameters().setImages(imagesFromActiveSnapshot);
            List<DiskImage> imagesIntersection = new ArrayList<>();
            for (DiskImage imageFromActive : imagesFromActiveSnapshot) {
                for (DiskImage imageFromPreview : imagesFromPreviewSnapshot) {
                    if (imageFromPreview.getId().equals(imageFromActive.getId())) {
                        imagesIntersection.add(imageFromPreview);
                    }
                }
                if (!imagesIntersection.isEmpty()) {
                    if (imageFromActive.getDiskStorageType() == DiskStorageType.IMAGE) {
                        imagesToRestore.add(imageFromActive);
                    } else if (imageFromActive.getDiskStorageType() == DiskStorageType.CINDER) {
                        CinderDisk cinderVolume = getInitialCinderVolumeToDelete(imageFromActive);
                        if (cinderVolume != null) {
                            imagesToRestore.add(cinderVolume);
                        }
                    }
                }
            }
            updateSnapshotIdForSkipRestoreImages(
                    ImagesHandler.imagesSubtract(imagesFromActiveSnapshot, imagesToRestore), activeSnapshotId);
            break;
        default:
            throw new EngineException(EngineError.ENGINE, "No support for restoring to snapshot type: "
                    + targetSnapshot.getType());
        }
    }

    private void updateSnapshotIdForSkipRestoreImages(List<DiskImage> skipRestoreImages, Guid snapshotId) {
        for (DiskImage image : skipRestoreImages) {
            getImageDao().updateImageVmSnapshotId(image.getImageId(), snapshotId);
        }
    }

    /**
     * Prepare to remove the active snapshot & restore the given snapshot to be the active one, including the
     * configuration.
     *
     * @param targetSnapshot
     *            The snapshot to restore to.
     */
    private void restoreConfiguration(Snapshot targetSnapshot) {
        SnapshotsManager snapshotsManager = new SnapshotsManager();
        removedSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        snapshotsToRemove.add(removedSnapshotId);
        snapshotsManager.removeAllIllegalDisks(removedSnapshotId, getVmId());

        snapshotsManager.attempToRestoreVmConfigurationFromSnapshot(getVm(),
                targetSnapshot,
                targetSnapshot.getId(),
                null,
                getCompensationContext(), getVm().getVdsGroupCompatibilityVersion(), getCurrentUser());
        getSnapshotDao().remove(targetSnapshot.getId());
        // add active snapshot with status locked, so that other commands that depend on the VM's snapshots won't run in parallel
        snapshotsManager.addActiveSnapshot(targetSnapshot.getId(),
                getVm(),
                SnapshotStatus.LOCKED,
                targetSnapshot.getMemoryVolume(),
                getCompensationContext());
    }

    /**
     * All snapshots who derive from the snapshot which is {@link SnapshotStatus#IN_PREVIEW}, up to it (excluding), will
     * be queued for deletion.<br>
     * The traversal between snapshots is done according to the {@link DiskImage} level.
     */
    protected void prepareToDeletePreviewBranch(List<DiskImage> imagesFromActiveSnapshot) {
        removedSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.PREVIEW);
        Guid previewedSnapshotId =
                getSnapshotDao().getId(getVmId(), SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW);
        getSnapshotDao().updateStatus(previewedSnapshotId, SnapshotStatus.OK);
        snapshotsToRemove.add(removedSnapshotId);
        List<DiskImage> images = getDiskImageDao().getAllSnapshotsForVmSnapshot(removedSnapshotId);
        for (DiskImage image : images) {
            if (image.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage parentImage = getDiskImageDao().getSnapshotById(image.getParentId());
                Guid snapshotToRemove = (parentImage == null) ? null : parentImage.getVmSnapshotId();

                while (parentImage != null && snapshotToRemove != null && !snapshotToRemove.equals(previewedSnapshotId)) {
                    snapshotsToRemove.add(snapshotToRemove);

                    parentImage = getDiskImageDao().getSnapshotById(parentImage.getParentId());
                    snapshotToRemove = (parentImage == null) ? null : parentImage.getVmSnapshotId();
                }
            }
        }
        addRedundantCinderSnapshots(previewedSnapshotId, imagesFromActiveSnapshot);
    }

    private void addRedundantCinderSnapshots(Guid previewedSnapshotId, List<DiskImage> imagesFromActiveSnapshot) {
        List<CinderDisk> cinderImagesForPreviewedSnapshot =
                ImagesHandler.filterDisksBasedOnCinder(getDiskImageDao().getAllSnapshotsForVmSnapshot(previewedSnapshotId));
        for (DiskImage image : cinderImagesForPreviewedSnapshot) {
            for (DiskImage diskImage : imagesFromActiveSnapshot) {
                if (diskImage.getId().equals(image.getId())) {
                    List<Guid> redundantSnapshotIdsToDelete =
                            CINDERStorageHelper.getRedundantVolumesToDeleteAfterCommitSnapshot(image.getId(),
                                    diskImage.getImageId());
                    snapshotsToRemove.addAll(redundantSnapshotIdsToDelete);
                }
            }
        }
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.RestoreFromSnapshot;
    }

    private List<DiskImage> getImagesList() {
        if (getParameters().getImages() == null && !getSnapshot().getId().equals(Guid.Empty)) {
            getParameters().setImages(getDiskImageDao().getAllSnapshotsForVmSnapshot(getSnapshot().getId()));
        }
        return getParameters().getImages();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_RESTORE_FROM_SNAPSHOT_START
                    : AuditLogType.USER_FAILED_RESTORE_FROM_SNAPSHOT;
        default:
            return AuditLogType.USER_RESTORE_FROM_SNAPSHOT_FINISH_SUCCESS;
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            if (getSnapshot() != null) {
                jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), snapshot.getDescription());
            }
        }
        return jobProperties;
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        SnapshotsValidator snapshotValidator = createSnapshotValidator();
        if (!validate(snapshotValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotValidator.snapshotExists(getVmId(), getSnapshot().getId())) ||
                !validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }
        if (Guid.Empty.equals(getSnapshot().getId())) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
        }
        VmValidator vmValidator = createVmValidator(getVm());

        MultipleStorageDomainsValidator storageValidator = createStorageDomainValidator();
        if (!validate(storageValidator.allDomainsExistAndActive()) ||
                !validate(storageValidator.allDomainsWithinThresholds()) ||
                !performImagesChecks() ||
                !validate(vmValidator.vmDown()) ||
                // if the user choose to commit a snapshot the vm can't have disk snapshots attached to other vms.
                (getSnapshot()).getType() == SnapshotType.REGULAR && !validate(vmValidator.vmNotHavingDeviceSnapshotsAttachedToOtherVms(false))) {
            return false;
        }

        if (getSnapshot().getType() == SnapshotType.REGULAR
                && getSnapshot().getStatus() != SnapshotStatus.IN_PREVIEW) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_NOT_IN_PREVIEW);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__REVERT_TO);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
    }

    protected SnapshotsValidator createSnapshotValidator() {
        return new SnapshotsValidator();
    }

    protected VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }

    protected MultipleStorageDomainsValidator createStorageDomainValidator() {
        Set<Guid> storageIds = ImagesHandler.getAllStorageIdsForImageIds(getImagesList());
        return new MultipleStorageDomainsValidator(getStoragePoolId(), storageIds);
    }

    protected boolean performImagesChecks() {
        List<DiskImage> diskImagesToCheck =
                ImagesHandler.filterImageDisks(getImagesList(), true, false, true);
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImagesToCheck);
        return validate(diskImagesValidator.diskImagesNotLocked());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        //
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        List<DiskImage> disks = getImagesList();

        if (disks != null && !disks.isEmpty()) {
            // TODO: need to be fixed. sp id should be available
            setStoragePoolId(disks.get(0).getStoragePoolId());

            for (DiskImage image : disks) {
                if (!image.getImage().isActive() && image.getQuotaId() != null
                        && !Guid.Empty.equals(image.getQuotaId())) {
                    list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(), null,
                            QuotaConsumptionParameter.QuotaAction.RELEASE,
                            image.getStorageIds().get(0),
                            image.getActualSize()));
                }
            }
        }

        return list;
    }

    @Override
    protected void endVmCommand() {
        unlockSnapshot(getParameters().getSnapshotId());
        super.endVmCommand();
    }
}
