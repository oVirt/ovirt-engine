package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A callback for commands that are executing their child commands serially. Note that this callback supports execution
 * of child commands until a failure or until successful completion.
 */
public class SerialChildCommandsExecutionCallback extends ChildCommandsCallbackBase {

    private static final Logger log = LoggerFactory.getLogger(SerialChildCommandsExecutionCallback.class);

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {
        Guid cmdId = command.getCommandId();
        SerialChildExecutingCommand serialChildExecutingCommand = (SerialChildExecutingCommand) command;
        if (!anyFailed || serialChildExecutingCommand.ignoreChildCommandFailure()) {
            try {
                boolean endCommand = !serialChildExecutingCommand.performNextOperation(completedChildren);
                if (!endCommand) {
                    return;
                }
            } catch (Exception e) {
                log.info("Command '{}' id: '{}' failed when attempting to perform the next operation, marking as FAILED '{}'",
                        command.getActionType(),
                        cmdId,
                        childCmdIds,
                        command.getCommandStatus());
                serialChildExecutingCommand.handleFailure();
                anyFailed = true;
            }
        } else {
            serialChildExecutingCommand.handleFailure();
        }

        setCommandEndStatus(command, anyFailed, status, childCmdIds);
    }
}
