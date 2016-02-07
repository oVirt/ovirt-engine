package org.ovirt.engine.core.bll.snapshots;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveDiskSnapshotsCommandCallback extends CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(RemoveDiskSnapshotsCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        boolean anyFailed = false;
        int completedChildren = 0;
        for (Guid childCmdId : childCmdIds) {
            switch (CommandCoordinatorUtil.getCommandStatus(childCmdId)) {
            case NOT_STARTED:
            case ACTIVE:
                log.info("Waiting on Live Merge child commands to complete");
                return;
            case FAILED:
            case ENDED_WITH_FAILURE:
            case EXECUTION_FAILED:
            case UNKNOWN:
                anyFailed = true;
                break;
            default:
                CommandEntity cmdEntity = CommandCoordinatorUtil.getCommandEntity(childCmdId);
                if (cmdEntity != null && (cmdEntity.isCallbackNotified() || !cmdEntity.isCallbackEnabled())) {
                    ++completedChildren;
                    break;
                } else {
                    log.info("Waiting on Live Merge child command to finalize");
                    return;
                }
            }
        }

        RemoveDiskSnapshotsCommand<RemoveDiskSnapshotsParameters> command = getCommand(cmdId);
        if (!anyFailed && completedChildren < command.getParameters().getImageIds().size()) {
            command.startNextLiveMerge(completedChildren);
            return;
        }

        command.getParameters().setTaskGroupSuccess(!anyFailed);
        command.setCommandStatus(anyFailed ? CommandStatus.FAILED : CommandStatus.SUCCEEDED);
        log.info("All Live Merge child commands have completed, status '{}'",
                command.getCommandStatus());
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    private RemoveDiskSnapshotsCommand<RemoveDiskSnapshotsParameters> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
