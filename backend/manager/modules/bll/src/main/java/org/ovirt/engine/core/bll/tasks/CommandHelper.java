package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.compat.CommandStatus;

public class CommandHelper {

    public static ActionReturnValue validate(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext commandContext) {
        return validate(actionType, parameters, commandContext, false);
    }

    public static ActionReturnValue validate(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext commandContext,
            boolean runAsInternal) {
         CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, commandContext);
         if (runAsInternal) {
             command.setInternalExecution(true);
         }
         return command.validateOnly();
    }

    public static CommandBase<?> buildCommand(ActionType actionType,
                                              ActionParametersBase parameters,
                                              ExecutionContext executionContext,
                                              CommandStatus cmdStatus) {
        ExecutionContext cmdExecutionContext = executionContext == null ? new ExecutionContext() : executionContext;
        CommandBase<?> command = CommandsFactory.createCommand(actionType,
                parameters,
                new CommandContext(new EngineContext()).withExecutionContext(cmdExecutionContext));
        command.setCommandStatus(cmdStatus, false);
        return command;
    }
}
