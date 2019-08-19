package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.CommandHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.MergeStatusReturnValue;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskStep;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class RemoveSnapshotSingleDiskLiveCommand<T extends RemoveSnapshotSingleDiskParameters>
        extends RemoveSnapshotSingleDiskCommandBase<T> implements SerialChildExecutingCommand {
    private static final Logger log = LoggerFactory.getLogger(RemoveSnapshotSingleDiskLiveCommand.class);

    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(RemoveSnapshotSingleDiskLiveCommandCallback.class)
    private Instance<RemoveSnapshotSingleDiskLiveCommandCallback> callbackProvider;

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
        ActionReturnValue actionReturnValue = null;

        if (currentChildId != null) {
            actionReturnValue = commandCoordinatorUtil.getCommandReturnValue(currentChildId);
            getParameters().setCommandStep(getParameters().getNextCommandStep());
        }

        log.info("Executing Live Merge command step '{}'", getParameters().getCommandStep());

        Pair<ActionType, ? extends ActionParametersBase> nextCommand = null;
        switch (getParameters().getCommandStep()) {
        case EXTEND:
            nextCommand = new Pair<>(ActionType.MergeExtend, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.MERGE);
            break;
        case MERGE:
            nextCommand = new Pair<>(ActionType.Merge, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.MERGE_STATUS);
            break;
        case MERGE_STATUS:
            nextCommand = new Pair<>(ActionType.MergeStatus, buildMergeParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.DESTROY_IMAGE);
            break;
        case DESTROY_IMAGE:
            if (actionReturnValue != null) {
                getParameters().setMergeStatusReturnValue(actionReturnValue.getActionReturnValue());
            } else if (getParameters().getMergeStatusReturnValue() == null) {
                // If the images were already merged, just add the orphaned image
                getParameters().setMergeStatusReturnValue(synthesizeMergeStatusReturnValue());
            }
            nextCommand = buildDestroyCommand(ActionType.DestroyImage, getActionType(),
                    new ArrayList<>(getParameters().getMergeStatusReturnValue().getImagesToRemove()));
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.REDUCE_IMAGE);
            break;
        case REDUCE_IMAGE:
            if (shouldSkipReduceImage()) {
                log.info("No need to execute reduce image command, skipping its execution. " +
                                "Storage Type: '{}', Disk: '{}' Snapshot: '{}'",
                        getStorageDomain().getStorageType(),
                        getImage().getName(),
                        getImage().getDescription());
                setCommandStatus(CommandStatus.SUCCEEDED);
            } else {
                Pair<ActionType, ? extends ActionParametersBase> reduceImageCommand =
                        buildReduceImageCommand();
                ActionReturnValue returnValue = CommandHelper.validate(reduceImageCommand.getFirst(),
                        reduceImageCommand.getSecond(), getContext().clone());
                if (returnValue.isValid()) {
                    nextCommand = reduceImageCommand;
                    getParameters().setNextCommandStep(RemoveSnapshotSingleDiskStep.COMPLETE);
                } else {
                    // Couldn't validate reduce image command for execution, however, we don't
                    // want to fail the remove snapshot command as this step isn't mandatory.
                    log.warn("Couldn't validate reduce image command, skipping its execution.");
                    setCommandStatus(CommandStatus.SUCCEEDED);
                }
            }
            break;
        case COMPLETE:
            setCommandStatus(CommandStatus.SUCCEEDED);
            break;
        }

        persistCommand(getParameters().getParentCommand(), true);
        if (nextCommand != null) {
            commandCoordinatorUtil.executeAsyncCommand(nextCommand.getFirst(), nextCommand.getSecond(), cloneContextAndDetachFromParent());
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

    private boolean shouldSkipReduceImage() {
        // Skipping file domains and live merge scenarios.
        return getStorageDomain().getStorageType().isFileDomain() ||
                (getActiveDiskImage() == null || getActiveDiskImage().getParentId().equals(getImageId()));
    }

    private boolean completedMerge() {
        return getParameters().getCommandStep() == RemoveSnapshotSingleDiskStep.DESTROY_IMAGE
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
        parameters.setParentCommand(ActionType.RemoveSnapshotSingleDiskLive);
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    private Pair<ActionType, ImagesActionsParametersBase> buildReduceImageCommand() {
        ImagesActionsParametersBase parameters = new ImagesActionsParametersBase(getDiskImage().getImageId());
        parameters.setParentCommand(ActionType.RemoveSnapshotSingleDiskLive);
        parameters.setParentParameters(getParameters());
        return new Pair<>(ActionType.ReduceImage, parameters);
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
        return new MergeStatusReturnValue(images);
    }

    public void onSucceeded() {
        syncDbRecords(getTargetImageInfoFromVdsm(), getParameters().getMergeStatusReturnValue().getImagesToRemove(), true);
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
        return getImageInfoFromVdsm(getDiskImage());
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
            syncDbRecords(getTargetImageInfoFromVdsm(),
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
        return callbackProvider.get();
    }

}
