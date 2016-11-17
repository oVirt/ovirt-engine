package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.MergeStatusReturnValue;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskStep;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class RemoveSnapshotSingleDiskLiveCommand<T extends RemoveSnapshotSingleDiskParameters>
        extends RemoveSnapshotSingleDiskCommandBase<T> implements SerialChildExecutingCommand {
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

    @Override
    public void handleFailure() {
        log.error("Command id: '{} failed child command status for step '{}'",
                getCommandId(),
                getParameters().getCommandStep());
    }

    @Override
    public boolean ignoreChildCommandFailure() {
        if (getParameters().getCommandStep() == RemoveSnapshotSingleDiskStep.DESTROY_IMAGE) {
            // It's possible that the image was destroyed already if this is retry of Live
            // Merge for the given volume.  Proceed to check if the image is present.
            log.warn("Child command '{}' failed, proceeding to verify", getParameters().getCommandStep());
            return true;
        }
        return false;
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {

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
        syncChildCommandList(getParameters());
        Guid currentChildId = getCurrentChildId(getParameters());
        VdcReturnValueBase vdcReturnValue = null;

        if (currentChildId != null) {
            vdcReturnValue = CommandCoordinatorUtil.getCommandReturnValue(currentChildId);
            getParameters().setCommandStep(getParameters().getNextCommandStep());
        }

        log.info("Executing Live Merge command step '{}'", getParameters().getCommandStep());

        Pair<VdcActionType, ? extends VdcActionParametersBase> nextCommand = null;
        switch (getParameters().getCommandStep()) {
        case EXTEND:
            nextCommand = new Pair<>(VdcActionType.MergeExtend, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.MERGE);
            break;
        case MERGE:
            nextCommand = new Pair<>(VdcActionType.Merge, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.MERGE_STATUS);
            break;
        case MERGE_STATUS:
            getParameters().setMergeCommandComplete(true);
            nextCommand = new Pair<>(VdcActionType.MergeStatus, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.DESTROY_IMAGE);
            break;
        case DESTROY_IMAGE:
            if (vdcReturnValue != null) {
                getParameters().setMergeStatusReturnValue(vdcReturnValue.getActionReturnValue());
            } else if (getParameters().getMergeStatusReturnValue() == null) {
                // If the images were already merged, just add the orphaned image
                getParameters().setMergeStatusReturnValue(synthesizeMergeStatusReturnValue());
            }
            nextCommand = buildDestroyCommand(VdcActionType.DestroyImage, getActionType(),
                    new ArrayList<>(getParameters().getMergeStatusReturnValue().getImagesToRemove()));
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.DESTROY_IMAGE_CHECK);
            break;
        case DESTROY_IMAGE_CHECK:
            nextCommand = buildDestroyCommand(VdcActionType.DestroyImageCheck, getActionType(),
                    new ArrayList<>(getParameters().getMergeStatusReturnValue().getImagesToRemove()));
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.COMPLETE);
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
            return true;
        } else {
            return false;
        }
    }

    private RemoveSnapshotSingleDiskStep getInitialMergeStepForImage(Guid imageId) {
        Image image = imageDao.get(imageId);
        if (image.getStatus() == ImageStatus.ILLEGAL
                && image.getParentId().equals(Guid.Empty)) {
            List<DiskImage> children = diskImageDao.getAllSnapshotsForParent(imageId);
            if (children.isEmpty()) {
                // An illegal, orphaned image means its contents have been merged
                log.info("Image has been previously merged, proceeding with deletion");
                return RemoveSnapshotSingleDiskStep.DESTROY_IMAGE;
            }
        }
        return RemoveSnapshotSingleDiskStep.EXTEND;
    }

    private boolean completedMerge() {
        return getParameters().getCommandStep() == RemoveSnapshotSingleDiskStep.DESTROY_IMAGE
                || getParameters().getCommandStep() == RemoveSnapshotSingleDiskStep.DESTROY_IMAGE_CHECK
                || getParameters().getCommandStep() == RemoveSnapshotSingleDiskStep.COMPLETE;
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
        syncDbRecords(getParameters().getMergeStatusReturnValue().getBlockJobType(),
                getTargetImageInfoFromVdsm(),
                getParameters().getMergeStatusReturnValue().getImagesToRemove(),
                true);
        log.info("Successfully merged snapshot '{}' images '{}'..'{}'",
                getDiskImage().getImage().getSnapshotId(),
                getDiskImage().getImageId(),
                getDestinationDiskImage() != null ? getDestinationDiskImage().getImageId() : "(n/a)");
    }

    private void syncDbRecordsMergeFailure() {
        DiskImage curr = getDestinationDiskImage();
        while (!curr.getImageId().equals(getDiskImage().getImageId())) {
            curr = diskImageDao.getSnapshotById(curr.getParentId());
            imageDao.updateStatus(curr.getImageId(), ImageStatus.ILLEGAL);
        }
    }

    private DiskImage getTargetImageInfoFromVdsm() {
        VmBlockJobType blockJobType = getParameters().getMergeStatusReturnValue().getBlockJobType();
        return getImageInfoFromVdsm(blockJobType == VmBlockJobType.PULL ? getDestinationDiskImage() : getDiskImage());
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
            syncDbRecords(getParameters().getMergeStatusReturnValue().getBlockJobType(),
                    getTargetImageInfoFromVdsm(),
                    getParameters().getMergeStatusReturnValue().getImagesToRemove(),
                    false);
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
