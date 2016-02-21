package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;

/**
 * A callback for commands that are executing their child commands serially. Note that this callback supports execution
 * of child commands until a failure or until successful completion.
 */
public class SerialChildCommandsExecutionCallback extends ConcurrentChildCommandsExecutionCallback {

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {
        SerialChildExecutingCommand serialChildExecutingCommand = (SerialChildExecutingCommand) command;
        if (!anyFailed || serialChildExecutingCommand.ignoreChildCommandFailure()) {
            try {
                boolean endCommand = !serialChildExecutingCommand.performNextOperation(completedChildren);
                if (!endCommand) {
                    return;
                }
            } catch (Exception e) {
                serialChildExecutingCommand.handleFailure();
                throw e;
            }
        } else {
            serialChildExecutingCommand.handleFailure();
        }
    }
}
