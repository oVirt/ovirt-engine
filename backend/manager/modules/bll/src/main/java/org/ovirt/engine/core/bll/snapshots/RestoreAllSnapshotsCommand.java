package org.ovirt.engine.core.bll.snapshots;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DeleteAllVmCheckpointsParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.RestoreAllCinderSnapshotsParameters;
import org.ovirt.engine.core.common.action.RestoreAllManagedBlockSnapshotsParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

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

    @Inject
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    protected VmCheckpointDao vmCheckpointDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private final Set<Guid> snapshotsToRemove = new HashSet<>();
    private Snapshot snapshot;
    List<DiskImage> imagesToRestore = new ArrayList<>();
    List<DiskImage> imagesFromPreviewSnapshot = new ArrayList<>();
    private Guid activeBeforeSnapshotLeaseDomainId;
    private Guid previewedSnapshotLeaseDomainId;

    /**
     * The snapshot which will be removed (the stateless/preview/active image).
     */
    private Snapshot removedSnapshot;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public RestoreAllSnapshotsCommand(Guid commandId) {
        super(commandId);
    }

    public RestoreAllSnapshotsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
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
        initializeSnapshotsLeasesDomainIds();

        restoreSnapshotAndRemoveObsoleteSnapshots(getSnapshot());

        boolean succeeded = removeLeaseIfNeeded();

        List<CinderDisk> cinderDisksToRestore = new ArrayList<>();
        List<ManagedBlockStorageDisk> managedBlockStorageDisksToRestore = new ArrayList<>();
        for (DiskImage image : imagesToRestore) {
            if (image.getImageStatus() != ImageStatus.ILLEGAL) {
                if (image.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                    managedBlockStorageDisksToRestore.add((ManagedBlockStorageDisk) image);
                    continue;
                }

                if (image.getDiskStorageType() == DiskStorageType.CINDER) {
                    cinderDisksToRestore.add((CinderDisk) image);
                    continue;
                }
                ImagesContainterParametersBase params = new RestoreFromSnapshotParameters(image.getImageId(),
                        getVmId(), getSnapshot(), removedSnapshot.getId());
                ActionReturnValue returnValue = runAsyncTask(ActionType.RestoreFromSnapshot, params);
                // Save the first fault
                if (succeeded && !returnValue.getSucceeded()) {
                    succeeded = false;
                    getReturnValue().setFault(returnValue.getFault());
                }
            }
        }

        List<CinderDisk> cinderVolumesToRemove = new ArrayList<>();
        List<CinderDisk> cinderDisksToRemove = new ArrayList<>();
        removeUnusedImages(cinderVolumesToRemove);

        if (getSnapshot().getType() == SnapshotType.REGULAR) {
            snapshotsToRemove.addAll(findSnapshotsWithOnlyIllegalDisks());
            setNewerVmConfigurationsAsBroken();
        }

        removeSnapshotsFromDB();
        // Deleting snapshots modifies the checkpoints,
        // so they cannot be used for future incremental backups.
        if (getParameters().getSnapshotAction() == SnapshotActionEnum.COMMIT) {
            invalidateAndRemoveAllVmCheckpoints();
        }
        succeeded = updateLeaseInfoIfNeeded() && succeeded;

        if (shouldInvokeChildCommand(cinderDisksToRestore, cinderVolumesToRemove, managedBlockStorageDisksToRestore)) {
            restoreManagedBlockSnapshotIfNeeded(managedBlockStorageDisksToRestore);
            deleteOrphanedImages(cinderDisksToRemove);
            if (!restoreCinderDisksIfNeeded(removedSnapshot.getId(),
                    cinderDisksToRestore,
                    cinderDisksToRemove,
                    cinderVolumesToRemove)) {
                log.error("Error to restore Cinder volumes snapshots");
            }
        } else {
            vmStaticDao.incrementDbGeneration(getVm().getId());
            snapshotDao.updateStatus(getSnapshot().getId(), SnapshotStatus.OK);
            unlockVm();
        }

        setSucceeded(succeeded);
    }

    // Check whether we should invoke additional commands based on the disk type
    private boolean shouldInvokeChildCommand(List<CinderDisk> cinderDisksToRestore,
            List<CinderDisk> cinderVolumesToRemove,
            List<ManagedBlockStorageDisk> managedBlockStorageDisksToRestore) {
        return !getTaskIdList().isEmpty() || !cinderDisksToRestore.isEmpty() || !cinderVolumesToRemove.isEmpty() ||
                !managedBlockStorageDisksToRestore.isEmpty();
    }

    private void restoreManagedBlockSnapshotIfNeeded(List<ManagedBlockStorageDisk> images) {
        if (!images.isEmpty()) {
            RestoreAllManagedBlockSnapshotsParameters params = new RestoreAllManagedBlockSnapshotsParameters();
            params.setManagedBlockStorageDisks(images);
            params.setSnapshotAction(getParameters().getSnapshotAction());
            params.setParentCommand(getActionType());
            params.setParentParameters(getParameters());
            params.setEndProcedure(EndProcedure.COMMAND_MANAGED);

            runInternalAction(ActionType.RestoreAllManagedBlockSnapshots, params);
        }
    }

    private void initializeSnapshotsLeasesDomainIds() {
        previewedSnapshotLeaseDomainId = getVm().getLeaseStorageDomainId();
        Snapshot activeBeforeSnapshot = snapshotDao.get(getVmId(), SnapshotType.PREVIEW);
        // activeBeforeSnapshot can be null in case of restore of stateless VM
        if (activeBeforeSnapshot != null && activeBeforeSnapshot.getVmConfiguration() != null) {
            activeBeforeSnapshotLeaseDomainId = OvfUtils.fetchLeaseDomainId(activeBeforeSnapshot.getVmConfiguration());
        }
    }

    private boolean removeLeaseIfNeeded() {
        if (isRemoveLeaseNeeded(activeBeforeSnapshotLeaseDomainId, previewedSnapshotLeaseDomainId)) {
            // remove the lease which created for the previewed snapshot or ,in case of commit, the
            // lease of the active before snapshot
            Guid leaseDomainIdToRemove = getParameters().getSnapshotAction() == SnapshotActionEnum.UNDO ?
                    previewedSnapshotLeaseDomainId : activeBeforeSnapshotLeaseDomainId;
            if (!removeVmLease(leaseDomainIdToRemove, getVmId())) {
                return false;
            }
        }
        return true;
    }

    private boolean isRemoveLeaseNeeded(Guid srcLeaseDomainId, Guid dstLeaseDomainId) {
        switch (getParameters().getSnapshotAction()) {
        case UNDO:
            return !Objects.equals(srcLeaseDomainId, dstLeaseDomainId) ||
                    (srcLeaseDomainId == null && dstLeaseDomainId != null);
        case COMMIT:
            return !Objects.equals(srcLeaseDomainId, dstLeaseDomainId) ||
                    (srcLeaseDomainId != null && dstLeaseDomainId == null);
        default:
            return false;
        }
    }

    private void invalidateAndRemoveAllVmCheckpoints() {
        // When a snapshot is committed and restored we cannot tell which
        // checkpoint was taken on which snapshot. Invalidate all the VM previous
        // checkpoints, so a full VM backup should be taken after restoring a snapshot.
        List<VmCheckpoint> vmCheckpoints = vmCheckpointDao.getAllForVm(getVmId());

        if (vmCheckpoints != null && !vmCheckpoints.isEmpty()) {
            log.info("Invalidating all VM '{}' checkpoints, full VM backup is now needed.", getVmName());
            TransactionSupport.executeInNewTransaction(() -> {
                vmCheckpointDao.invalidateAllCheckpointsByVmId(getVmId());
                return null;
            });
            log.info("Removing all VM '{}' checkpoints.", getVmName());
            removeAllVmCheckpoints(vmCheckpoints);
        }
    }

    private void removeAllVmCheckpoints(List<VmCheckpoint> vmCheckpoints) {
        log.info("Removing VM '{}' checkpoints.", getVmName());

        // Collect all the images that were part of a backup.
        List<DiskImage> imagesWithCheckpoints = vmCheckpoints.stream()
                .map(vmCheckpoint -> vmCheckpointDao.getDisksByCheckpointId(vmCheckpoint.getId()))
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        DeleteAllVmCheckpointsParameters deleteAllVmCheckpointsParameters =
                new DeleteAllVmCheckpointsParameters(getVmId(), imagesWithCheckpoints);
        deleteAllVmCheckpointsParameters.setParentCommand(getActionType());
        deleteAllVmCheckpointsParameters.setParentParameters(getParameters());
        deleteAllVmCheckpointsParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);

        runInternalAction(ActionType.DeleteAllVmCheckpoints, deleteAllVmCheckpointsParameters);
    }

    private boolean updateLeaseInfoIfNeeded() {
        if (isLeaseInfoUpdateNeeded(activeBeforeSnapshotLeaseDomainId, previewedSnapshotLeaseDomainId)) {
            return getParameters().getSnapshotAction() == SnapshotActionEnum.UNDO ?
                    updateLeaseInfo(activeBeforeSnapshotLeaseDomainId) : updateLeaseInfo(previewedSnapshotLeaseDomainId);
        }
        return true;
    }

    private boolean isLeaseInfoUpdateNeeded(Guid srcLeaseDomainId, Guid dstLeaseDomainId) {
        return (getParameters().getSnapshotAction() == SnapshotActionEnum.UNDO
                && !(srcLeaseDomainId == null && dstLeaseDomainId == null)) || (
                getParameters().getSnapshotAction() == SnapshotActionEnum.COMMIT && (srcLeaseDomainId != null
                        && dstLeaseDomainId == null));
    }

    private boolean updateLeaseInfo(Guid snapshotLeaseDomainId) {
        if (snapshotLeaseDomainId == null) {
            // there was no lease for the snapshot
            vmDynamicDao.updateVmLeaseInfo(getParameters().getVmId(), null);
            return true;
        }

        VDSReturnValue retVal = null;
        try {
            retVal = runVdsCommand(VDSCommandType.GetVmLeaseInfo,
                    new VmLeaseVDSParameters(getStoragePoolId(),
                            snapshotLeaseDomainId,
                            getParameters().getVmId()));
        } catch (EngineException e) {
            log.error("Failure in getting lease info for VM {}, message: {}",
                    getParameters().getVmId(), e.getMessage());
        }

        if (retVal == null || !retVal.getSucceeded()) {
            log.error("Failed to get info on the lease of VM {}", getParameters().getVmId());
            return false;
        }

        vmDynamicDao.updateVmLeaseInfo(
                getParameters().getVmId(),
                (Map<String, String>) retVal.getReturnValue());

        return true;
    }

    protected boolean restoreAllCinderDisks(List<CinderDisk> cinderDisksToRestore,
                                            List<CinderDisk> cinderDisksToRemove,
                                            List<CinderDisk> cinderVolumesToRemove,
                                            Guid removedSnapshotId) {
        Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                ActionType.RestoreAllCinderSnapshots,
                        buildCinderChildCommandParameters(cinderDisksToRestore,
                                cinderDisksToRemove,
                                cinderVolumesToRemove,
                                removedSnapshotId),
                cloneContextAndDetachFromParent());
        try {
            ActionReturnValue actionReturnValue = future.get();
            if (!actionReturnValue.getSucceeded()) {
                getReturnValue().setFault(actionReturnValue.getFault());
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
        List<DiskImage> snapshotsForParent = diskImageDao.getAllSnapshotsForParent(cinderVolume.getParentId());
        Optional<DiskImage> cinderVolumeToRemove = snapshotsForParent.stream().filter(snapshot ->
                !snapshot.getImageId().equals(cinderVolume.getImageId())).map(Optional::ofNullable).findFirst().orElse(null);
        return cinderVolumeToRemove != null ? (CinderDisk) cinderVolumeToRemove.get() : null;
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
        restoreParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return withRootCommandInfo(restoreParams);
    }

    private Snapshot getSnapshot() {
        if (snapshot == null) {
            switch (getParameters().getSnapshotAction()) {
            case UNDO:
                snapshot = snapshotDao.get(getVmId(), SnapshotType.PREVIEW);
                break;
            case COMMIT:
                snapshot = snapshotDao.get(getVmId(), SnapshotStatus.IN_PREVIEW);
                break;
            case RESTORE_STATELESS:
                snapshot = snapshotDao.get(getVmId(), SnapshotType.STATELESS);
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
            Snapshot snap = snapshotDao.get(snapshotId);
            // Cinder volumes might not have correlated snapshot.
            if (snap != null) {
                if (snap.containsMemory() && snapshotDao.getNumOfSnapshotsByDisks(snap) == 1) {
                    // Best effort to remove memory disks
                    RemoveMemoryVolumesParameters params = new RemoveMemoryVolumesParameters(snap, getVmId(), true);
                    params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
                    commandCoordinatorUtil.executeAsyncCommand(
                            ActionType.RemoveMemoryVolumes,
                            withRootCommandInfo(params),
                            cloneContextAndDetachFromParent());
                }
                snapshotDao.remove(snapshotId);
            }
        }
    }

    private boolean isSnapshotEligibleToBeDeleted(Snapshot candidateSnapshotToRemove) {
        return candidateSnapshotToRemove != null
                && (candidateSnapshotToRemove.getType() != SnapshotType.REGULAR ||
                candidateSnapshotToRemove.getCreationDate().getTime() > removedSnapshot.getCreationDate().getTime());
    }

    protected void deleteOrphanedImages(List<CinderDisk> cinderDisksToRemove) {
        ActionReturnValue returnValue;
        boolean noImagesRemovedYet = getTaskIdList().isEmpty();
        Set<Guid> deletedDisksIds = new HashSet<>();
        for (DiskImage image : diskImageDao.getImagesWithNoDisk(getVm().getId())) {
            if (!deletedDisksIds.contains(image.getId())) {
                deletedDisksIds.add(image.getId());
                if (image.getDiskStorageType() == DiskStorageType.CINDER) {
                    cinderDisksToRemove.add((CinderDisk) image);
                    continue;
                }
                returnValue = runAsyncTask(ActionType.RemoveImage,
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

    private boolean restoreCinderDisksIfNeeded(Guid removedSnapshotId, List<CinderDisk> cinderDisksToRestore,
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
            List<DiskImage> snapshotDiskImages = diskImageDao.getAllSnapshotsForVmSnapshot(snapshotToRemove);
            imagesToRemove.addAll(snapshotDiskImages);
        }

        Set<Guid> removeInProcessImageIds = new HashSet<>();
        for (DiskImage diskImage : imagesToRemove) {
            if (imageIdsUsedByActiveSnapshot.contains(diskImage.getId()) ||
                    removeInProcessImageIds.contains(diskImage.getId())) {
                continue;
            }

            List<DiskImage> diskImagesFromPreviewSnap = imagesFromPreviewSnapshot.stream().filter(diskImageFromPreview ->
                    diskImageFromPreview.getImageId().equals(diskImage.getImageId())).collect(Collectors.toList());
            if (!diskImagesFromPreviewSnap.isEmpty() &&
                    diskImagesFromPreviewSnap.get(0).getDiskStorageType() == DiskStorageType.CINDER) {
                cinderVolumesToRemove.add((CinderDisk) diskImagesFromPreviewSnap.get(0));
                continue;
            }
            ActionReturnValue retValue = runAsyncTask(ActionType.RemoveImage,
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
    private ActionReturnValue runAsyncTask(ActionType taskType, ImagesContainterParametersBase params) {
        ActionReturnValue returnValue;
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
        Guid activeSnapshotId = snapshotDao.getId(getVmId(), SnapshotType.ACTIVE);
        List<DiskImage> imagesFromActiveSnapshot = diskImageDao.getAllSnapshotsForVmSnapshot(activeSnapshotId);

        Snapshot previewedSnapshot = snapshotDao.get(getVmId(), SnapshotType.PREVIEW);
        if (previewedSnapshot != null) {
            VM vmFromConf = snapshotVmConfigurationHelper.getVmFromConfiguration(previewedSnapshot);
            List<DiskImage> previewedImagesFromDB = diskImageDao.getAllSnapshotsForVmSnapshot(previewedSnapshot.getId());
            imagesFromPreviewSnapshot.addAll(ImagesHandler.imagesIntersection(vmFromConf.getImages(), previewedImagesFromDB));
        }
        List<DiskImage> intersection = ImagesHandler.imagesIntersection(imagesFromActiveSnapshot, imagesFromPreviewSnapshot);

        switch (targetSnapshot.getType()) {
        case PREVIEW:
            snapshotDao.updateStatus(
                    snapshotDao.getId(getVmId(), SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW),
                    SnapshotStatus.OK);

            getParameters().setImages((List<DiskImage>) CollectionUtils.union(imagesFromPreviewSnapshot, intersection));
            imagesFromPreviewSnapshot.forEach(image -> {
                if (image.getDiskStorageType() != DiskStorageType.CINDER
                        && image.getDiskStorageType() != DiskStorageType.MANAGED_BLOCK_STORAGE) {
                    imagesToRestore.add(image);
                } else {
                    List<DiskImage> cinderDiskFromPreviewSnapshot = intersection.stream().filter(diskImage->
                            diskImage.getId().equals(image.getId())).collect(Collectors.toList());
                    if (!cinderDiskFromPreviewSnapshot.isEmpty()) {
                        imagesToRestore.add(cinderDiskFromPreviewSnapshot.get(0));
                    }
                }
            });
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
            imagesFromActiveSnapshot.forEach(image -> {
                List<DiskImage> cinderDiskFromPreviewSnapshot = imagesFromPreviewSnapshot.stream().filter(diskImage->
                        diskImage.getId().equals(image.getId())).collect(Collectors.toList());
                if (!cinderDiskFromPreviewSnapshot.isEmpty()) {
                    if (image.getDiskStorageType() == DiskStorageType.IMAGE ||
                            image.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                        imagesToRestore.add(image);
                    } else if (image.getDiskStorageType() == DiskStorageType.CINDER){
                        CinderDisk cinderVolume = getInitialCinderVolumeToDelete(image);
                        if (cinderVolume != null) {
                            imagesToRestore.add(cinderVolume);
                        }
                    }
                }
            });
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
            imageDao.updateImageVmSnapshotId(image.getImageId(), snapshotId);
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
        removedSnapshot = snapshotDao.get(getVmId(), SnapshotType.ACTIVE);
        snapshotsToRemove.add(removedSnapshot.getId());
        getSnapshotsManager().removeAllIllegalDisks(removedSnapshot.getId(), getVmId());

        getSnapshotsManager().attemptToRestoreVmConfigurationFromSnapshot(getVm(),
                targetSnapshot,
                targetSnapshot.getId(),
                null,
                getCompensationContext(),
                getCurrentUser(),
                new VmInterfaceManager(getMacPool()),
                targetSnapshot.containsMemory());
        snapshotDao.remove(targetSnapshot.getId());
        // add active snapshot with status locked, so that other commands that depend on the VM's snapshots won't run in parallel
        getSnapshotsManager().addActiveSnapshot(targetSnapshot.getId(),
                getVm(),
                SnapshotStatus.LOCKED,
                targetSnapshot.getMemoryDiskId(),
                targetSnapshot.getMetadataDiskId(),
                getCompensationContext());
    }

    /**
     * All snapshots who derive from the snapshot which is {@link SnapshotStatus#IN_PREVIEW}, up to it (excluding), will
     * be queued for deletion.<br>
     * The traversal between snapshots is done according to the {@link DiskImage} level.
     */
    protected void prepareToDeletePreviewBranch(List<DiskImage> imagesFromActiveSnapshot) {
        removedSnapshot = snapshotDao.get(getVmId(), SnapshotType.PREVIEW);
        Guid previewedSnapshotId =
                snapshotDao.getId(getVmId(), SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW);
        snapshotDao.updateStatus(previewedSnapshotId, SnapshotStatus.OK);
        snapshotsToRemove.add(removedSnapshot.getId());
        addRedundantCinderSnapshots(previewedSnapshotId, imagesFromActiveSnapshot);
    }

    private void addRedundantCinderSnapshots(Guid previewedSnapshotId, List<DiskImage> imagesFromActiveSnapshot) {
        List<CinderDisk> cinderImagesForPreviewedSnapshot =
                DisksFilter.filterCinderDisks(diskImageDao.getAllSnapshotsForVmSnapshot(previewedSnapshotId));
        Set<Guid> criticalSnapshotsChain = getCriticalSnapshotsChain(imagesFromActiveSnapshot, cinderImagesForPreviewedSnapshot);
        for (DiskImage image : cinderImagesForPreviewedSnapshot) {
            List<Guid> redundantSnapshotIdsToDelete =
                    getRedundantVolumesToDeleteAfterCommitSnapshot(image.getId(), criticalSnapshotsChain);
            snapshotsToRemove.addAll(redundantSnapshotIdsToDelete.stream()
                    .filter(snapIdToDelete -> isSnapshotEligibleToBeDeleted(snapshotDao.get(snapIdToDelete)))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * A utility method for committing a previewed snapshot. The method filters out all the snapshots which will not be
     * part of volume chain once the snapshot get committed, and returns a list of redundant snapshots that should be
     * deleted.
     *
     * @param diskId
     *            - Disk id to fetch all volumes related to it.
     * @param criticalSnapshotsChain
     *            - The snapshot's ids which are critical for the VM since they are used and can not be deleted.
     * @return - A list of redundant snapshots that should be deleted.
     */
    private List<Guid> getRedundantVolumesToDeleteAfterCommitSnapshot(Guid diskId, Set<Guid> criticalSnapshotsChain) {
        List<Guid> redundantSnapshotIdsToDelete = new ArrayList<>();

        // Fetch all the relevant snapshots to remove.
        List<DiskImage> allVolumesInCinderDisk = diskImageDao.getAllSnapshotsForImageGroup(diskId);
        for (DiskImage diskImage : allVolumesInCinderDisk) {
            if (!criticalSnapshotsChain.contains(diskImage.getVmSnapshotId())) {
                redundantSnapshotIdsToDelete.add(diskImage.getVmSnapshotId());
            }
        }
        return redundantSnapshotIdsToDelete;
    }

    private Set<Guid> getCriticalSnapshotsChain(List<DiskImage> imagesFromActiveSnapshot, List<CinderDisk> cinderImagesForPreviewedSnapshot) {
        Set<Guid> criticalSnapshotsChain = new HashSet<>();
        for (DiskImage image : cinderImagesForPreviewedSnapshot) {
            List<DiskImage> cinderDiskFromSnapshot = imagesFromActiveSnapshot.stream().filter(diskImage->
                    diskImage.getId().equals(image.getId())).collect(Collectors.toList());
            for (DiskImage diskImage : diskImageDao.getAllSnapshotsForLeaf(cinderDiskFromSnapshot.get(0).getImageId())) {
                criticalSnapshotsChain.add(diskImage.getVmSnapshotId());
            }
        }
        return criticalSnapshotsChain;
    }

    private Set<Guid> findSnapshotsWithOnlyIllegalDisks() {
        List<Snapshot> newerSnapshots = getNewerSnapshots(snapshot);
        Set<Guid> snapshotsToRemove = new HashSet<>();

        newerSnapshots.forEach(snapshot -> {
            VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(snapshot);
            if (vm != null) {
                boolean shouldRemove = vm.getImages().isEmpty() || vm.getImages().stream().allMatch(
                        diskImage -> diskImage.getImageStatus() == ImageStatus.ILLEGAL);
                if (shouldRemove) {
                    snapshotsToRemove.add(snapshot.getId());
                }
            }
        });

        return snapshotsToRemove;
    }

    private void setNewerVmConfigurationsAsBroken() {
        List<Snapshot> newerSnapshots = getNewerSnapshots(snapshot);

        newerSnapshots.forEach(newerSnapshot -> {
            newerSnapshot.setVmConfigurationBroken(true);
            snapshotDao.update(newerSnapshot);
        });
    }

    private List<Snapshot> getNewerSnapshots(Snapshot snapshot) {
        return snapshotDao.getAllWithConfiguration(getVmId()).stream().filter(
                snapshotFromDao ->
                        snapshotFromDao.getType() == SnapshotType.REGULAR &&
                                snapshotFromDao.getCreationDate().getTime() > snapshot.getCreationDate().getTime())
                .collect(Collectors.toList());
    }

    @Override
    protected ActionType getChildActionType() {
        return ActionType.RestoreFromSnapshot;
    }

    private List<DiskImage> getImagesList() {
        if (getParameters().getImages() == null && !getSnapshot().getId().equals(Guid.Empty)) {
            getParameters().setImages(diskImageDao.getAllSnapshotsForVmSnapshot(getSnapshot().getId()));
        }
        return getParameters().getImages();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (getSucceeded()) {
                if (getParameters().getSnapshotAction() == SnapshotActionEnum.UNDO) {
                    return AuditLogType.USER_UNDO_RESTORE_FROM_SNAPSHOT_START;
                }
                return AuditLogType.USER_COMMIT_RESTORE_FROM_SNAPSHOT_START;
            }
            if (getParameters().getSnapshotAction() == SnapshotActionEnum.UNDO) {
                return AuditLogType.USER_UNDO_RESTORE_FROM_SNAPSHOT_FINISH_FAILURE;
            }
            return AuditLogType.USER_COMMIT_RESTORE_FROM_SNAPSHOT_FINISH_FAILURE;
        default:
            if (getParameters().getSnapshotAction() == SnapshotActionEnum.UNDO) {
                return AuditLogType.USER_UNDO_RESTORE_FROM_SNAPSHOT_FINISH_SUCCESS;
            }
            return AuditLogType.USER_COMMIT_RESTORE_FROM_SNAPSHOT_FINISH_SUCCESS;
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
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!validate(snapshotsValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotsValidator.snapshotExists(getVmId(), getSnapshot().getId())) ||
                !validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
            return false;
        }
        if (Guid.Empty.equals(getSnapshot().getId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
        }
        VmValidator vmValidator = createVmValidator(getVm());

        MultipleStorageDomainsValidator storageValidator = createStorageDomainValidator();
        if (!validate(storageValidator.allDomainsExistAndActive()) ||
                !performImagesChecks() ||
                !validate(vmValidator.vmDown()) ||
                !validate(storageValidator.isSupportedByManagedBlockStorageDomains(getActionType())) ||
                // if the user choose to commit a snapshot the vm can't have disk snapshots attached to other vms.
                getSnapshot().getType() == SnapshotType.REGULAR && !validate(vmValidator.vmNotHavingDeviceSnapshotsAttachedToOtherVms(false))) {
            return false;
        }

        if (getSnapshot().getType() == SnapshotType.REGULAR
                && getSnapshot().getStatus() != SnapshotStatus.IN_PREVIEW) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_NOT_IN_PREVIEW);
        }

        if(!canRestoreVmConfigFromSnapshot()) {
            return failValidation(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
        }

        return true;
    }

    private boolean canRestoreVmConfigFromSnapshot() {
        Snapshot snapshot = getSnapshot();
        return snapshot.getType() == SnapshotType.PREVIEW ?
                getSnapshotsManager().canRestoreVmConfigurationFromSnapshot(getVm(),
                        snapshot,
                        new VmInterfaceManager(getMacPool()))
                : true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REVERT_TO);
        addValidationMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
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
                DisksFilter.filterImageDisks(getImagesList(), ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
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
                    list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(),
                            QuotaConsumptionParameter.QuotaAction.RELEASE,
                            image.getStorageIds().get(0),
                            image.getActualSize()));
                }
            }
        }

        return list;
    }

    @Override
    protected void endSuccessfully() {
        unlockSnapshot(getParameters().getSnapshotId());
        super.endVmCommand();
    }

    @Override
    protected void endWithFailure() {
        super.endVmCommand();
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
