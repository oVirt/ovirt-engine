package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.tasks.TaskManagerUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.MergeStatusReturnValue;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskLiveStep;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Image;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RemoveSnapshotSingleDiskLiveCommand<T extends RemoveSnapshotSingleDiskParameters>
        extends RemoveSnapshotSingleDiskCommandBase<T> {
    private static final Log log = LogFactory.getLog(RemoveSnapshotSingleDiskLiveCommand.class);

    public RemoveSnapshotSingleDiskLiveCommand(T parameters) {
        super(parameters);
    }

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
            getParameters().setChildCommands(new HashMap<RemoveSnapshotSingleDiskLiveStep, Guid>());
        }

        List<Guid> childCommandIds = TaskManagerUtil.getChildCommandIds(getCommandId());
        if (childCommandIds.size() != getParameters().getChildCommands().size()) {
            // Upon recovery or after invoking a new child command, our map may be missing an entry
            for (Guid id : childCommandIds) {
                if (!getParameters().getChildCommands().containsValue(id)) {
                    getParameters().getChildCommands().put(getParameters().getCommandStep(), id);
                    break;
                }
            }
        }
        Guid currentChildId = getParameters().getChildCommands().get(
                getParameters().getCommandStep());

        VdcReturnValueBase vdcReturnValue = null;
        if (currentChildId != null) {
            switch (TaskManagerUtil.getCommandStatus(currentChildId)) {
            case ACTIVE:
            case ACTIVE_ASYNC:
            case ACTIVE_SYNC:
            case NOT_STARTED:
                log.infoFormat("Waiting on Live Merge command step {0} to complete",
                        getParameters().getCommandStep());
                return;

            case SUCCEEDED:
                vdcReturnValue = TaskManagerUtil.getCommandReturnValue(currentChildId);
                if (vdcReturnValue != null && vdcReturnValue.getSucceeded()) {
                    getParameters().setCommandStep(getParameters().getNextCommandStep());
                    break;
                } else {
                    log.errorFormat("Child command {0} failed: {1}",
                            getParameters().getCommandStep(),
                            (vdcReturnValue != null
                                    ? vdcReturnValue.getExecuteFailedMessages()
                                    : "null return value")
                    );
                    setCommandStatus(CommandStatus.FAILED);
                    return;
                }

            case FAILED:
            case FAILED_RESTARTED:
                log.errorFormat("Failed child command status for step {0}",
                        getParameters().getCommandStep());
                setCommandStatus(CommandStatus.FAILED);
                return;

            case UNKNOWN:
                log.errorFormat("Unknown child command status for step {0}",
                        getParameters().getCommandStep());
                setCommandStatus(CommandStatus.FAILED);
                return;
            }
        }

        log.infoFormat("Executing Live Merge command step {0}", getParameters().getCommandStep());

        Pair<VdcActionType, ? extends VdcActionParametersBase> nextCommand = null;
        switch (getParameters().getCommandStep()) {
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
                getParameters().setMergeStatusReturnValue((MergeStatusReturnValue) vdcReturnValue.getActionReturnValue());
            } else if (getParameters().getMergeStatusReturnValue() == null) {
                // If the images were already merged, just add the orphaned image
                getParameters().setMergeStatusReturnValue(synthesizeMergeStatusReturnValue());
            }
            nextCommand = new Pair<>(VdcActionType.DestroyImage, buildDestroyImageParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskLiveStep.COMPLETE);
            break;
        case COMPLETE:
            getParameters().setDestroyImageCommandComplete(true);
            setCommandStatus(CommandStatus.SUCCEEDED);
            break;
        }

        persistCommandWithContext(getParameters().getParentCommand(), true);
        if (nextCommand != null) {
            TaskManagerUtil.executeAsyncCommand(nextCommand.getFirst(), nextCommand.getSecond(), cloneContextAndDetachFromParent());
        }
    }

    private RemoveSnapshotSingleDiskLiveStep getInitialMergeStepForImage(Guid imageId) {
        Image image = getImageDao().get(imageId);
        if (image.getStatus() == ImageStatus.ILLEGAL
                && (image.getParentId().equals(Guid.Empty))) {
            List<DiskImage> children = DbFacade.getInstance().getDiskImageDao()
                    .getAllSnapshotsForParent(imageId);
            if (children.isEmpty()) {
                // An illegal, orphaned image means its contents have been merged
                log.info("Image has been previously merged, proceeding with deletion");
                return RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE;
            }
        }
        return RemoveSnapshotSingleDiskLiveStep.MERGE;
    }

    private boolean completedMerge() {
        return getParameters().getCommandStep() == RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE
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
                new ArrayList<Guid>(getParameters().getMergeStatusReturnValue().getImagesToRemove()),
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
        syncDbRecords(true);
        endSuccessfully();
        log.infoFormat("Successfully merged snapshot {0} images {1}..{2}",
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

    /**
     * After merging the snapshots, update the image and snapshot records in the
     * database to reflect the changes.  This handles either forward or backwards
     * merge (detected).  It will either then remove the images, or mark them
     * illegal (to handle the case where image deletion failed).
     *
     * @param removeImages Remove the images from the database, or if false, only
     *                     mark them illegal
     */
    private void syncDbRecords(boolean removeImages) {
        // If deletion failed after a backwards merge, the snapshots' images need to be swapped
        // as they would upon success.  Instead of removing them, mark them illegal.
        DiskImage baseImage = getDiskImage();
        DiskImage topImage = getDestinationDiskImage();

        // The vdsm merge verb may decide to perform a forward or backward merge.
        if (topImage == null) {
            log.debug("No merge destination image, not updating image/snapshot association");
        } else if (getParameters().getMergeStatusReturnValue().getBlockJobType() == VmBlockJobType.PULL) {
            // For forward merge, the volume format and type may change.
            topImage.setvolumeFormat(baseImage.getVolumeFormat());
            topImage.setVolumeType(baseImage.getVolumeType());
            topImage.setParentId(baseImage.getParentId());
            topImage.setImageStatus(ImageStatus.OK);

            getBaseDiskDao().update(topImage);
            getImageDao().update(topImage.getImage());
            updateDiskImageDynamic(topImage);
        } else {
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
            baseImage.getImage().setActive(oldTopIsActive);

            topImage.setImageStatus(ImageStatus.OK);
            getBaseDiskDao().update(topImage);
            getImageDao().update(topImage.getImage());
            updateDiskImageDynamic(topImage);

            getBaseDiskDao().update(baseImage);
            getImageDao().update(baseImage.getImage());

            updateVmConfigurationForImageChange(topImage.getImage().getSnapshotId(),
                    baseImage.getImageId(), topImage);
        }

        Set<Guid> imagesToUpdate = getParameters().getMergeStatusReturnValue().getImagesToRemove();
        if (imagesToUpdate == null) {
            log.error("Failed to update orphaned images in db: image list could not be retrieved");
            return;
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
    }

    private void updateVmConfigurationForImageChange(final Guid snapshotId, final Guid oldImageId, final DiskImage newImage) {
        try {
            VM vm = getVm();
            lockVmSnapshotsWithWait(vm);

            TransactionSupport.executeInNewTransaction(
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            Snapshot s = getSnapshotDao().get(snapshotId);
                            s = ImagesHandler.prepareSnapshotConfigWithAlternateImage(s, oldImageId, newImage);
                            getSnapshotDao().update(s);
                            return null;
                        }
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

    public void onFailed() {
        if (!completedMerge()) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    syncDbRecordsMergeFailure();
                    return null;
                }
            });
            log.errorFormat("Merging of snapshot {0} images {1}..{2} failed. Images have been" +
                            " marked illegal and can no longer be previewed or reverted to." +
                            " Please retry Live Merge on the snapshot to complete the operation.",
                    getDiskImage().getImage().getSnapshotId(),
                    getDiskImage().getImageId(),
                    getDestinationDiskImage() != null ? getDestinationDiskImage().getImageId() : "(n/a)"
            );

        } else {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    syncDbRecords(false);
                    return null;
                }
            });
            log.errorFormat("Snapshot {0} images {1}..{2} merged, but volume removal failed." +
                            " Some or all of the following volumes may be orphaned: {3}." +
                            " Please retry Live Merge on the snapshot to complete the operation.",
                    getDiskImage().getImage().getSnapshotId(),
                    getDiskImage().getImageId(),
                    getDestinationDiskImage() != null ? getDestinationDiskImage().getImageId() : "(n/a)",
                    getParameters().getMergeStatusReturnValue().getImagesToRemove());
        }
        endWithFailure();
    }

    @Override
    public CommandCallBack getCallBack() {
        return new RemoveSnapshotSingleDiskLiveCommandCallback();
    }
}
