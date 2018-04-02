package org.ovirt.engine.core.bll;

import java.util.List;

import javax.enterprise.inject.Typed;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A callback for commands that are executing their child commands serially. Note that this callback supports execution
 * of child commands until a failure or until successful completion.
 */
@Typed(SerialChildCommandsExecutionCallback.class)
public class SerialChildCommandsExecutionCallback extends ChildCommandsCallbackBase {

    private static final Logger log = LoggerFactory.getLogger(SerialChildCommandsExecutionCallback.class);

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {
        Guid cmdId = command.getCommandId();
        if (status == CommandExecutionStatus.EXECUTED) {
            SerialChildExecutingCommand serialChildExecutingCommand = (SerialChildExecutingCommand) command;
            if (!anyFailed || serialChildExecutingCommand.ignoreChildCommandFailure()) {
                try {
                    boolean endCommand = !serialChildExecutingCommand.performNextOperation(completedChildren);
                    if (!endCommand) {
                        return;
                    }
                } catch (Exception e) {
                    log.error("Command '{}' id: '{}' with children {} failed when attempting to perform the next operation, marking as '{}'",
                            command.getActionType(),
                            cmdId,
                            childCmdIds,
                            command.getCommandStatus());
                    log.error(e.getMessage(), e);
                    serialChildExecutingCommand.handleFailure();
                    anyFailed = true;
                }
            } else {
                serialChildExecutingCommand.handleFailure();
            }
        } else {
            log.info("Command '{}' id: '{}' execution didn't complete, not proceeding to perform the next operation",
                    command.getActionType(),
                    cmdId);
        }

        setCommandEndStatus(command, anyFailed, status, childCmdIds);
    }
}
