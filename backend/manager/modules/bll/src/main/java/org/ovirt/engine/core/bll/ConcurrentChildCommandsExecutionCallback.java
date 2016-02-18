package org.ovirt.engine.core.bll;

import java.util.List;

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

        setCommandEndStatus(command, anyFailed, status, childCmdIds);
    }
}
