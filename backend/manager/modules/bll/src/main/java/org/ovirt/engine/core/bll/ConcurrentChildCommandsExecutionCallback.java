package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;

/**
 * A callback that should be used by commands that execute number of child commands concurrently. When the execution of
 * child commands is over, the end method of the commands is called.
 */
public class ConcurrentChildCommandsExecutionCallback extends ChildCommandsCallbackBase {

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        command.getParameters().setTaskGroupSuccess(!anyFailed && status == CommandExecutionStatus.EXECUTED);
        command.setCommandStatus(command.getParameters().getTaskGroupSuccess() ? CommandStatus.SUCCEEDED
                : CommandStatus.FAILED);
        log.info("Command '{}' id: '{}' child commands '{}' executions were completed, status '{}'",
                command.getActionType(), command.getCommandId(), childCmdIds, command.getCommandStatus());
    }
}
