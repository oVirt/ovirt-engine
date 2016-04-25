package org.ovirt.engine.core.bll;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.utils.CorrelationIdTracker;

@Singleton
public class NestedCommandFactory {

    public CommandBase<?> createWrappedCommand(CommandContext commandContext, VdcActionType actionType,
            VdcActionParametersBase parameter, boolean isInternal) {
        CommandBase<?> command = isInternal ?
                CommandsFactory.createCommand(actionType, parameter,
                        commandContext != null ? commandContext.clone().withoutCompensationContext() : null) :
                CommandsFactory.createCommand(actionType, parameter);
        command.setInternalExecution(isInternal);
        return command;
    }

    public void prepareCommandForMonitoring(CommandContext commandContext, CommandBase<?> command) {
        CorrelationIdTracker.setCorrelationId(command.getCorrelationId());
        if (commandContext == null || commandContext.getExecutionContext() == null
                || commandContext.getExecutionContext().isMonitored()) {
            ExecutionHandler.prepareCommandForMonitoring(command,
                    command.getActionType(),
                    command.isInternalExecution());
        }
    }
}
