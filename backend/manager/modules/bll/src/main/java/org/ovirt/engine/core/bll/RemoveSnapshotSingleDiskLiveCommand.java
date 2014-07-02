package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

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
            getParameters().setCommandStep(RemoveSnapshotSingleDiskLiveStep.MERGE);
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
                    log.errorFormat("Failed to merge, child command {0} failed: {1}",
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
                log.errorFormat("Failed to merge: failed child command status for step {0}",
                        getParameters().getCommandStep());
                setCommandStatus(CommandStatus.FAILED);
                return;

            case UNKNOWN:
                log.errorFormat("Failed to merge: unknown child command status for step {0}",
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
            getParameters().setMergeStatusReturnValue((MergeStatusReturnValue) vdcReturnValue.getActionReturnValue());
            nextCommand =
                    new Pair<>(VdcActionType.DestroyImage, buildDestroyImageParameters());
            getParameters().setNextCommandStep(RemoveSnapshotSingleDiskLiveStep.COMPLETE);
            break;
        case COMPLETE:
            getParameters().setDestroyImageCommandComplete(true);
            setCommandStatus(CommandStatus.SUCCEEDED);
            break;
        }

        persistCommand(getParameters().getParentCommand(), true);
        if (nextCommand != null) {
            TaskManagerUtil.executeAsyncCommand(nextCommand.getFirst(), nextCommand.getSecond());
        }
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

    public void onSucceeded() {
        DiskImage baseImage = getDiskImage();
        DiskImage topImage = getDestinationDiskImage();

        // The vdsm merge verb may decide to perform a forward or backward merge.
        if (getParameters().getMergeStatusReturnValue().getBlockJobType() == VmBlockJobType.PULL) {
            // For forward merge, the volume format and type may change.
            topImage.setvolumeFormat(baseImage.getVolumeFormat());
            topImage.setVolumeType(baseImage.getVolumeType());
            topImage.setParentId(baseImage.getParentId());
        } else {
            // For backwards merge, the prior base image now has the data associated with the newer
            // snapshot we want to keep.  Re-associate this older image with the newer snapshot.
            List<DiskImage> children = DbFacade.getInstance().getDiskImageDao()
                    .getAllSnapshotsForParent(topImage.getImageId());
            if (!children.isEmpty()) {
                DiskImage childImage = children.get(0);
                childImage.setParentId(baseImage.getImageId());
                getImageDao().update(childImage.getImage());
            }

            baseImage.getImage().setSnapshotId(topImage.getImage().getSnapshotId());
            topImage.setImage(baseImage.getImage());
        }
        getBaseDiskDao().update(topImage);
        getImageDao().update(topImage.getImage());
        updateDiskImageDynamic(topImage);

        Set<Guid> imagesToRemove = getParameters().getMergeStatusReturnValue().getImagesToRemove();
        if (imagesToRemove == null) {
            log.error("Failed to remove images from db: image list could not be retrieved");
            return;
        }
        for (Guid imageId : imagesToRemove) {
            DbFacade.getInstance().getImageDao().remove(imageId);
        }

        endSuccessfully();
        log.infoFormat("Successfully merged snapshot {0} images {1}..{2}",
                baseImage.getSnapshotId(), baseImage.getImageId(), topImage.getImageId());
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    public void onFailed() {
        endWithFailure();
    }

    @Override
    public CommandCallBack getCallBack() {
        return new RemoveSnapshotSingleDiskLiveCommandCallback();
    }
}
