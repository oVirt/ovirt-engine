package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.CloneCinderDisksParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneCinderDisksCommandCallback<T extends CommandBase<CloneCinderDisksParameters>> extends CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(CloneCinderDisksCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {

        boolean anyFailed = false;
        int finishedChildren = 0;
        for (Guid childCmdId : childCmdIds) {
            CommandStatus commandStatus = CommandCoordinatorUtil.getCommandStatus(childCmdId);
            switch (commandStatus) {
            case SUCCEEDED:
                finishedChildren++;
                break;
            case FAILED:
            case FAILED_RESTARTED:
            case UNKNOWN:
                anyFailed = true;
            default:
                finishedChildren++;
                log.error("Invalid command status: '{}", commandStatus);
                break;
            }
        }

        if (finishedChildren == childCmdIds.size()) {
            T command = getCommand(cmdId);
            command.getParameters().setTaskGroupSuccess(!anyFailed);
            command.setCommandStatus(anyFailed ? CommandStatus.FAILED : CommandStatus.SUCCEEDED);
            log.info("All CloneCinderDisksCommandCallback commands have completed, status '{}'",
                    command.getCommandStatus());
        } else {
            log.info("Waiting for all child commands to finish. {}/{} child commands were completed.",
                    childCmdIds.size(),
                    finishedChildren);
        }
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

    private T getCommand(Guid cmdId) {
        return (T) CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
