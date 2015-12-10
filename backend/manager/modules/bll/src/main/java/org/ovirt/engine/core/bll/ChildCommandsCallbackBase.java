package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChildCommandsCallbackBase extends CommandCallback {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        CommandExecutionStatus status = CommandCoordinatorUtil.getCommandExecutionStatus(cmdId);
        // TODO: should be removed when doPolling will be moved to run only after execute finish - here for test purpose
        // only.
        if (status != CommandExecutionStatus.EXECUTED &&
                CommandCoordinatorUtil.getCommandStatus(cmdId) == CommandStatus.ACTIVE) {
            return;
        }

        boolean anyFailed = false;
        int completedChildren = 0;
        CommandBase<?> command = getCommand(cmdId);
        for (Guid childCmdId : childCmdIds) {
            CommandBase<?> child = getCommand(childCmdId);
            switch (CommandCoordinatorUtil.getCommandStatus(childCmdId)) {
            case NOT_STARTED:
            case ACTIVE:
                logWaitingForChildCommand(child, command);
                return;
            case FAILED:
            case FAILED_RESTARTED:
                if (shouldWaitForEndMethodsCompletion(child, command)) {
                    return;
                }
                anyFailed = true;
                break;
            case ENDED_WITH_FAILURE:
            case UNKNOWN:
                anyFailed = true;
                break;
            case SUCCEEDED:
                if (shouldWaitForEndMethodsCompletion(child, command)) {
                    return;
                }
            default:
                ++completedChildren;
            }
        }

        childCommandsExecutionEnded(command, anyFailed, childCmdIds, status, completedChildren);
    }

    private boolean shouldWaitForEndMethodsCompletion(CommandBase<?> childCommand, CommandBase<?> parentCommand) {
        CommandEntity cmdEntity = CommandCoordinatorUtil.getCommandEntity(childCommand.getCommandId());
        boolean hasNotifiedCallback = cmdEntity.isCallbackEnabled() && cmdEntity.isCallbackNotified();

        if (!childCommand.getParameters().getShouldBeEndedByParent() && !hasNotifiedCallback) {
            logWaitingForChildCommand(childCommand, parentCommand);
            return true;
        }

        return false;
    }

    private void logWaitingForChildCommand(CommandBase<?> childCommand, CommandBase<?> parentCommand) {
        log.info("Command '{}' (id: '{}') waiting on child command id: '{}' type:'{}' to complete",
                parentCommand.getActionType(),
                parentCommand.getCommandId(),
                childCommand.getCommandId(),
                childCommand.getActionType());
    }

    protected abstract void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren);

    private void endAction(CommandBase<?> commandBase, List<Guid> childCmdIds, boolean succeeded) {
        if (commandBase.getParameters().getParentCommand() == VdcActionType.Unknown
                || !commandBase.getParameters().getShouldBeEndedByParent()) {

            commandBase.endAction();

            if (commandBase.getParameters().getParentCommand() == VdcActionType.Unknown) {
                CommandCoordinatorUtil.removeAllCommandsInHierarchy(commandBase.getCommandId());
            }

            ExecutionHandler.endJob(commandBase.getExecutionContext(), succeeded);
        }
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        endAction(getCommand(cmdId), childCmdIds, true);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        CommandBase<?> commandBase = getCommand(cmdId);
        // This should be removed as soon as infra bug will be fixed and failed execution will reach endWithFailure
        commandBase.getParameters().setTaskGroupSuccess(false);
        endAction(commandBase, childCmdIds, false);
    }

    protected CommandBase<?> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
