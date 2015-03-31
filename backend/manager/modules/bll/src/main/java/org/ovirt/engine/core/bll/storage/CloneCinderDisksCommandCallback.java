package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.CloneCinderDisksParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CloneCinderDisksCommandCallback<T extends CloneCinderDisksCommand<CloneCinderDisksParameters>> extends CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(CloneCinderDisksCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {

        boolean anyFailed = false;
        for (Guid childCmdId : childCmdIds) {
            CommandStatus commandStatus = CommandCoordinatorUtil.getCommandStatus(childCmdId);
            switch (commandStatus) {
            case ACTIVE:
                log.info("Waiting on CloneCinderDisksCommandCallback child commands to complete");
                return;
            case FAILED:
            case FAILED_RESTARTED:
            case UNKNOWN:
                anyFailed = true;
            default:
                log.error("Invalid command status: '{}", commandStatus);
                break;
            }
        }

        T command = getCommand(cmdId);
        command.getParameters().setTaskGroupSuccess(!anyFailed);
        command.setCommandStatus(anyFailed ? CommandStatus.FAILED : CommandStatus.SUCCEEDED);
        log.info("All CloneCinderDisksCommandCallback commands have completed, status '{}'",
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

    private T getCommand(Guid cmdId) {
        return (T) CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
