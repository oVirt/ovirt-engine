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
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.action.MergeStatusReturnValue;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskLiveStep;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
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
        if (getParameters().getCommandStep() == RemoveSnapshotSingleDiskLiveStep.DESTROY_IMAGE) {
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
        syncChildCommandList();
        Guid currentChildId = getCurrentChildId();
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
            return true;
        } else {
            return false;
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
            curr = getDbFacade().getDiskImageDao().getSnapshotById(curr.getParentId());
            getImageDao().updateStatus(curr.getImageId(), ImageStatus.ILLEGAL);
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
