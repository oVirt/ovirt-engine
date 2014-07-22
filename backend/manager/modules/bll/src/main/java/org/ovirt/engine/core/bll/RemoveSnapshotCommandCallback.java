package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.TaskManagerUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class RemoveSnapshotCommandCallback extends CommandCallBack {
    private static final Log log = LogFactory.getLog(RemoveSnapshotCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {

        boolean anyFailed = false;
        for (Guid childCmdId : childCmdIds) {
            switch (TaskManagerUtil.getCommandStatus(childCmdId)) {
            case ACTIVE:
                log.info("Waiting on Live Merge child commands to complete");
                return;
            case FAILED:
            case FAILED_RESTARTED:
            case UNKNOWN:
                anyFailed = true;
                break;
            default:
                break;
            }
        }

        RemoveSnapshotCommand<RemoveSnapshotParameters> command = getCommand(cmdId);
        command.getParameters().setTaskGroupSuccess(!anyFailed);
        command.setCommandStatus(anyFailed ? CommandStatus.FAILED : CommandStatus.SUCCEEDED);
        log.infoFormat("All Live Merge child commands have completed, status {0}",
                command.getCommandStatus());
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
        TaskManagerUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
        TaskManagerUtil.removeAllCommandsInHierarchy(cmdId);
    }

    private RemoveSnapshotCommand<RemoveSnapshotParameters> getCommand(Guid cmdId) {
        return (RemoveSnapshotCommand<RemoveSnapshotParameters>) TaskManagerUtil.retrieveCommand(cmdId);
    }
}
