package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class ParallelMultipleActionsRunner extends PrevalidatingMultipleActionsRunner {

    public ParallelMultipleActionsRunner(ActionType actionType,
            List<ActionParametersBase> parameters,
            CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected void invokeCommands() {
        runCommands();
    }

    @Override
    protected void runCommands() {
        for (final CommandBase<?> command : getCommands()) {
            if (command.getReturnValue().isValid()) {
                ThreadPoolUtil.execute(() -> executeValidatedCommand(command));
            }
        }
    }
}
