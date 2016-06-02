package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.MergeStatusReturnValue;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskLiveStep;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class RemoveSnapshotSingleDiskLiveCommand<T extends RemoveSnapshotSingleDiskParameters>
        extends RemoveSnapshotSingleDiskCommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(RemoveSnapshotSingleDiskLiveCommand.class);

    public RemoveSnapshotSingleDiskLiveCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        // Let doPolling() drive the execution; we don't have any guarantee that
        // executeCommand() will finish before doPolling() is called, and we don't
        // want to possibly run the first command twice.
        setSucceeded(true); // Allow runAction to succeed
    }

    public void proceedCommandExecution() {
        // Steps are executed such that:
        //  a) all logic before the command runs is idempotent
        //  b) the command is the last action in the step
        // This allows for recovery after a crash at any point during command execution.

        log.debug("Proceeding with execution of RemoveSnapshotSingleDiskLiveCommand");
        if (getParameters().getCommandStep() == null) {
            getParameters().setCommandStep(getInitialMergeStepForImage(getParameters().getImageId()));
            getParameters().setChildCommands(new HashMap<>());
        }

        // Upon recovery or after invoking a new child command, our map may be missing an entry
        syncChildCommandList();
        Guid currentChildId = getCurrentChildId();

        VdcReturnValueBase vdcReturnValue = null;
        if (currentChildId != null) {
            switch (CommandCoordinatorUtil.getCommandStatus(currentChildId)) {
            case ACTIVE:
            case NOT_STARTED:
                log.info("Waiting on Live Merge command step '{}' to complete",
                        getParameters().getCommandStep());
                return;

            case ENDED_SUCCESSFULLY:
            case SUCCEEDED:
                CommandEntity cmdEntity = CommandCoordinatorUtil.getCommandEntity(currentChildId);
                if (cmdEntity.isCallbackEnabled() && !cmdEntity.isCallbackNotified()) {
                    log.info("Waiting on Live Merge command step '{}' to finalize",
                            getParameters().getCommandStep());
                    return;
                }

                vdcReturnValue = CommandCoordinatorUtil.getCommandReturnValue(currentChildId);
                if (vdcReturnValue != null && vdcReturnValue.getSucceeded()) {
                    log.debug("Child command '{}' succeeded",
                            getParameters().getCommandStep());
                    getParameters().setCommandStep(getParameters().getNextCommandStep());
                    break;
                } else {
                    log.error("Child command '{}' failed: {}",
                            getParameters().getCommandStep(),
                            vdcReturnValue != null
                                    ? vdcReturnValue.getExecuteFailedMessages()
                                    : "null return value"
                    );
                    setCommandStatus(CommandStatus.FAILED);
                    return;
                }

            case FAILED:
            case ENDED_WITH_FAILURE:
            case EXECUTION_FAILED:
                if (getParameters().getCommandStep() == RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE) {
                    // It's possible that the image was destroyed already if this is retry of Live
                    // Merge for the given volume.  Proceed to check if the image is present.
                    log.warn("Child command '{}' failed, proceeding to verify", getParameters().getCommandStep());
                    getParameters().setCommandStep(getParameters().getNextCommandStep());
                    break;
                }
                log.error("Failed child command status for step '{}'",
                        getParameters().getCommandStep());
                setCommandStatus(CommandStatus.FAILED);
                return;

            case UNKNOWN:
                log.error("Unknown child command status for step '{}'",
                        getParameters().getCommandStep());
                setCommandStatus(CommandStatus.FAILED);
                return;
            }
        }

        log.info("Executing Live Merge command step '{}'", getParameters().getCommandStep());

        Pair<VdcActionType, ? extends VdcActionParametersBase> nextCommand = null;
        switch (getParameters().getCommandStep()) {
        case EXTEND:
            nextCommand = new Pair<>(VdcActionType.MergeExtend, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskLiveStep.MERGE);
            break;
        case MERGE:
            nextCommand = new Pair<>(VdcActionType.Merge, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskLiveStep.MERGE_STATUS);
            break;
        case MERGE_STATUS:
            getParameters().setMergeCommandComplete(true);
            nextCommand = new Pair<>(VdcActionType.MergeStatus, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE);
            break;
        case DESTROY_IMAGE:
            if (vdcReturnValue != null) {
                getParameters().setMergeStatusReturnValue(vdcReturnValue.getActionReturnValue());
            } else if (getParameters().getMergeStatusReturnValue() == null) {
                // If the images were already merged, just add the orphaned image
                getParameters().setMergeStatusReturnValue(synthesizeMergeStatusReturnValue());
            }
            nextCommand = new Pair<>(VdcActionType.DestroyImage, buildDestroyImageParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE_CHECK);
            break;
        case DESTROY_IMAGE_CHECK:
            nextCommand = new Pair<>(VdcActionType.DestroyImageCheck, buildDestroyImageParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskLiveStep.COMPLETE);
            break;
        case COMPLETE:
            getParameters().setDestroyImageCommandComplete(true);
            setCommandStatus(CommandStatus.SUCCEEDED);
            break;
        }

        persistCommand(getParameters().getParentCommand(), true);
        if (nextCommand != null) {
            CommandCoordinatorUtil.executeAsyncCommand(nextCommand.getFirst(), nextCommand.getSecond(), cloneContextAndDetachFromParent());
            // Add the child, but wait, it's a race!  child will start, task may spawn, get polled, and we won't have the child id
        }
    }

    /**
     * Updates (but does not persist) the parameters.childCommands list to ensure the current
     * child command is present.  This is necessary in various entry points called externally
     * (e.g. by endAction()), which can be called after a child command is started but before
     * the main proceedCommandExecution() loop has persisted the updated child list.
     */
    private void syncChildCommandList() {
        List<Guid> childCommandIds = CommandCoordinatorUtil.getChildCommandIds(getCommandId());
        if (childCommandIds.size() != getParameters().getChildCommands().size()) {
            for (Guid id : childCommandIds) {
                if (!getParameters().getChildCommands().containsValue(id)) {
                    getParameters().getChildCommands().put(getParameters().getCommandStep(), id);
                    break;
                }
            }
        }
    }

    private Guid getCurrentChildId() {
        return getParameters().getChildCommands().get(getParameters().getCommandStep());
    }

    private RemoveSnapshotSingleDiskLiveStep getInitialMergeStepForImage(Guid imageId) {
        Image image = getImageDao().get(imageId);
        if (image.getStatus() == ImageStatus.ILLEGAL
                && image.getParentId().equals(Guid.Empty)) {
            List<DiskImage> children = DbFacade.getInstance().getDiskImageDao()
                    .getAllSnapshotsForParent(imageId);
            if (children.isEmpty()) {
                // An illegal, orphaned image means its contents have been merged
                log.info("Image has been previously merged, proceeding with deletion");
                return RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE;
            }
        }
        return RemoveSnapshotSingleDiskLiveStep.EXTEND;
    }

    private boolean completedMerge() {
        return getParameters().getCommandStep() == RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE
                || getParameters().getCommandStep() == RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE_CHECK
                || getParameters().getCommandStep() == RemoveSnapshotSingleDiskLiveStep.COMPLETE;
    }

    private MergeParameters buildMergeParameters() {
        MergeParameters parameters = new MergeParameters(
                getVdsId(),
                getVmId(),
                getActiveDiskImage(),
                getDiskImage(),
                getDestinationDiskImage(),
                0);
        parameters.setParentCommand(VdcActionType.RemoveSnapshotSingleDiskLive);
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    private DestroyImageParameters buildDestroyImageParameters() {
        DestroyImageParameters parameters = new DestroyImageParameters(
                getVdsId(),
                getVmId(),
                getDiskImage().getStoragePoolId(),
                getDiskImage().getStorageIds().get(0),
                getActiveDiskImage().getId(),
                new ArrayList<>(getParameters().getMergeStatusReturnValue().getImagesToRemove()),
                getDiskImage().isWipeAfterDelete(),
                false);
        parameters.setParentCommand(VdcActionType.RemoveSnapshotSingleDiskLive);
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    private DiskImage getActiveDiskImage() {
        Guid snapshotId = getSnapshotDao().getId(getVmId(), Snapshot.SnapshotType.ACTIVE);
        return getDiskImageDao().getDiskSnapshotForVmSnapshot(getDiskImage().getId(), snapshotId);
    }

    /**
     * Add orphaned, already-merged images from this snapshot to a MergeStatusReturnValue that
     * can be used by the DESTROY_IMAGE command step to tell what needs to be deleted.
     *
     * @return A suitable MergeStatusReturnValue object
     */
    private MergeStatusReturnValue synthesizeMergeStatusReturnValue() {
        Set<Guid> images = new HashSet<>();
        images.add(getDiskImage().getImageId());
        return new MergeStatusReturnValue(VmBlockJobType.UNKNOWN, images);
    }

    public void onSucceeded() {
        syncDbRecords(getTargetImageInfoFromVdsm(), true);
        log.info("Successfully merged snapshot '{}' images '{}'..'{}'",
                getDiskImage().getImage().getSnapshotId(),
                getDiskImage().getImageId(),
                getDestinationDiskImage() != null ? getDestinationDiskImage().getImageId() : "(n/a)");
    }

    private void syncDbRecordsMergeFailure() {
        DiskImage curr = getDestinationDiskImage();
        while (!curr.getImageId().equals(getDiskImage().getImageId())) {
            curr = getDbFacade().getDiskImageDao().getSnapshotById(curr.getParentId());
            getImageDao().updateStatus(curr.getImageId(), ImageStatus.ILLEGAL);
        }
    }

    private DiskImage getTargetImageInfoFromVdsm() {
        VmBlockJobType blockJobType = getParameters().getMergeStatusReturnValue().getBlockJobType();
        return getImageInfoFromVdsm(blockJobType == VmBlockJobType.PULL ? getDestinationDiskImage() : getDiskImage());
    }

    /**
     * After merging the snapshots, update the image and snapshot records in the
     * database to reflect the changes.  This handles either forward or backwards
     * merge (detected).  It will either then remove the images, or mark them
     * illegal (to handle the case where image deletion failed).
     *
     * @param removeImages Remove the images from the database, or if false, only
     *                     mark them illegal
     */
    private void syncDbRecords(DiskImage imageFromVdsm, boolean removeImages) {
        TransactionSupport.executeInNewTransaction(() -> {
            // If deletion failed after a backwards merge, the snapshots' images need to be swapped
            // as they would upon success.  Instead of removing them, mark them illegal.
            DiskImage baseImage = getDiskImage();
            DiskImage topImage = getDestinationDiskImage();

            // The vdsm merge verb may decide to perform a forward or backward merge.
            if (topImage == null) {
                log.info("No merge destination image, not updating image/snapshot association");
            } else if (getParameters().getMergeStatusReturnValue().getBlockJobType() == VmBlockJobType.PULL) {
                handleForwardLiveMerge(topImage, baseImage, imageFromVdsm);
            } else {
                handleBackwardLiveMerge(topImage, baseImage, imageFromVdsm);
            }

            Set<Guid> imagesToUpdate = getParameters().getMergeStatusReturnValue().getImagesToRemove();
            if (imagesToUpdate == null) {
                log.error("Failed to update orphaned images in db: image list could not be retrieved");
                return null;
            }
            for (Guid imageId : imagesToUpdate) {
                if (removeImages) {
                    getImageDao().remove(imageId);
                } else {
                    // The (illegal && no-parent && no-children) status indicates an orphaned image.
                    Image image = getImageDao().get(imageId);
                    image.setStatus(ImageStatus.ILLEGAL);
                    image.setParentId(Guid.Empty);
                    getImageDao().update(image);
                }
            }
            return null;
        });
    }

    private void handleForwardLiveMerge(DiskImage topImage, DiskImage baseImage, DiskImage imageFromVdsm) {
        // For forward merge, the volume format and type may change.
        topImage.setVolumeFormat(baseImage.getVolumeFormat());
        topImage.setVolumeType(baseImage.getVolumeType());
        topImage.setParentId(baseImage.getParentId());
        topImage.setImageStatus(ImageStatus.OK);

        getBaseDiskDao().update(topImage);
        getImageDao().update(topImage.getImage());
        updateDiskImageDynamic(imageFromVdsm, topImage);

        updateVmConfigurationForImageRemoval(baseImage.getImage().getSnapshotId(),
                baseImage.getImageId());
    }

    private void handleBackwardLiveMerge(DiskImage topImage, DiskImage baseImage, DiskImage imageFromVdsm) {
        // For backwards merge, the prior base image now has the data associated with the newer
        // snapshot we want to keep.  Re-associate this older image with the newer snapshot.
        // The base snapshot is deleted if everything went well.  In case it's not deleted, we
        // hijack it to preserve a link to the broken image.  This makes the image discoverable
        // so that we can retry the deletion later, yet doesn't corrupt the VM image chain.
        List<DiskImage> children = DbFacade.getInstance().getDiskImageDao()
                .getAllSnapshotsForParent(topImage.getImageId());
        if (!children.isEmpty()) {
            DiskImage childImage = children.get(0);
            childImage.setParentId(baseImage.getImageId());
            getImageDao().update(childImage.getImage());
        }

        Image oldTopImage = topImage.getImage();
        topImage.setImage(baseImage.getImage());
        baseImage.setImage(oldTopImage);

        Guid oldTopSnapshotId = topImage.getImage().getSnapshotId();
        topImage.getImage().setSnapshotId(baseImage.getImage().getSnapshotId());
        baseImage.getImage().setSnapshotId(oldTopSnapshotId);

        boolean oldTopIsActive = topImage.getImage().isActive();
        topImage.getImage().setActive(baseImage.getImage().isActive());
        VolumeClassification baseImageVolumeClassification =
                VolumeClassification.getVolumeClassificationByActiveFlag(baseImage.getImage().isActive());
        topImage.getImage().setVolumeClassification(baseImageVolumeClassification);
        baseImage.getImage().setActive(oldTopIsActive);
        VolumeClassification oldTopVolumeClassification =
                VolumeClassification.getVolumeClassificationByActiveFlag(oldTopIsActive);
        topImage.getImage().setVolumeClassification(oldTopVolumeClassification);

        topImage.setSize(baseImage.getSize());
        topImage.setActualSizeInBytes(imageFromVdsm.getActualSizeInBytes());
        topImage.setImageStatus(ImageStatus.OK);
        getBaseDiskDao().update(topImage);
        getImageDao().update(topImage.getImage());
        updateDiskImageDynamic(imageFromVdsm, topImage);

        getBaseDiskDao().update(baseImage);
        getImageDao().update(baseImage.getImage());

        updateVmConfigurationForImageChange(topImage.getImage().getSnapshotId(),
                baseImage.getImageId(), topImage);
        updateVmConfigurationForImageRemoval(baseImage.getImage().getSnapshotId(),
                topImage.getImageId());
    }

    private void updateVmConfigurationForImageChange(final Guid snapshotId, final Guid oldImageId, final DiskImage newImage) {
        try {
            lockVmSnapshotsWithWait(getVm());

            TransactionSupport.executeInNewTransaction(() -> {
                        Snapshot s = getSnapshotDao().get(snapshotId);
                        s = ImagesHandler.prepareSnapshotConfigWithAlternateImage(s, oldImageId, newImage);
                        getSnapshotDao().update(s);
                        return null;
                    });
        } finally {
            if (getSnapshotsEngineLock() != null) {
                getLockManager().releaseLock(getSnapshotsEngineLock());
            }
        }
    }

    private void updateVmConfigurationForImageRemoval(final Guid snapshotId, final Guid imageId) {
        try {
            lockVmSnapshotsWithWait(getVm());

            TransactionSupport.executeInNewTransaction(() -> {
                        Snapshot s = getSnapshotDao().get(snapshotId);
                        s = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(s, imageId);
                        getSnapshotDao().update(s);
                        return null;
                    });
        } finally {
            if (getSnapshotsEngineLock() != null) {
                getLockManager().releaseLock(getSnapshotsEngineLock());
            }
        }
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    public void onFailed() {
        if (!completedMerge()) {
            TransactionSupport.executeInNewTransaction(() -> {
                syncDbRecordsMergeFailure();
                return null;
            });
            log.error("Merging of snapshot '{}' images '{}'..'{}' failed. Images have been" +
                            " marked illegal and can no longer be previewed or reverted to." +
                            " Please retry Live Merge on the snapshot to complete the operation.",
                    getDiskImage().getImage().getSnapshotId(),
                    getDiskImage().getImageId(),
                    getDestinationDiskImage() != null ? getDestinationDiskImage().getImageId() : "(n/a)"
            );

        } else {
            syncDbRecords(getTargetImageInfoFromVdsm(), false);
            log.error("Snapshot '{}' images '{}'..'{}' merged, but volume removal failed." +
                            " Some or all of the following volumes may be orphaned: {}." +
                            " Please retry Live Merge on the snapshot to complete the operation.",
                    getDiskImage().getImage().getSnapshotId(),
                    getDiskImage().getImageId(),
                    getDestinationDiskImage() != null ? getDestinationDiskImage().getImageId() : "(n/a)",
                    getParameters().getMergeStatusReturnValue().getImagesToRemove());
        }
    }

    @Override
    public CommandCallback getCallback() {
        return new RemoveSnapshotSingleDiskLiveCommandCallback();
    }
}
