package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChildCommandsCallbackBase implements CommandCallback {

    @Inject
    protected CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    private ExecutionHandler executionHandler;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        CommandExecutionStatus status = commandCoordinatorUtil.getCommandExecutionStatus(cmdId);
        // TODO: should be removed when doPolling will be moved to run only after execute finish - here for test purpose
        // only.
        if (status != CommandExecutionStatus.EXECUTED &&
                commandCoordinatorUtil.getCommandStatus(cmdId) == CommandStatus.ACTIVE) {
            return;
        }

        boolean anyFailed = false;
        int completedChildren = 0;
        CommandBase<?> command = getCommand(cmdId);
        for (Guid childCmdId : childCmdIds) {
            CommandBase<?> child = getCommand(childCmdId);
            switch (commandCoordinatorUtil.getCommandStatus(childCmdId)) {
            case NOT_STARTED:
            case ACTIVE:
                logWaitingForChildCommand(child, command);
                return;
            case EXECUTION_FAILED:
                if (shouldExecuteEndMethod(child)) {
                    logWaitingForChildCommand(child, command);
                    return;
                }
                logChildCommandFailed(cmdId, childCmdId, CommandStatus.EXECUTION_FAILED);
                anyFailed = true;
                break;
            case FAILED:
                if (shouldWaitForEndMethodsCompletion(child, command)) {
                    return;
                }
                logChildCommandFailed(cmdId, childCmdId, CommandStatus.FAILED);
                anyFailed = true;
                break;
            case ENDED_WITH_FAILURE:
            case UNKNOWN:
                logChildCommandFailed(cmdId, childCmdId, CommandStatus.UNKNOWN);
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

    private void logChildCommandFailed(Guid cmdId, Guid childCmdId, CommandStatus status){
        log.debug("Child command id: '{}' of command id: '{}' status is {}, set as failed.", childCmdId, cmdId, status);
    }

    private boolean shouldCommandEndOnAsyncOpEnd(CommandBase<?> cmd) {
        return cmd.getParameters().getEndProcedure() == EndProcedure.COMMAND_MANAGED;
    }

    private boolean shouldWaitForEndMethodsCompletion(CommandBase<?> childCommand, CommandBase<?> parentCommand) {
        CommandEntity cmdEntity = commandCoordinatorUtil.getCommandEntity(childCommand.getCommandId());
        boolean hasNotifiedCallback = cmdEntity.isCallbackEnabled() && cmdEntity.isCallbackNotified();

        if (shouldCommandEndOnAsyncOpEnd(childCommand) && !hasNotifiedCallback) {
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

    protected void setCommandEndStatus(CommandBase<?> command, boolean childCommandFailed,
                                       CommandExecutionStatus status, List<Guid> childCmdIds) {
        log.debug("Command '{}' id: '{}' marked as execution status '{}',",
                command.getActionType(),
                command.getCommandId(),
                status);
        command.getParameters().setTaskGroupSuccess(!childCommandFailed && status == CommandExecutionStatus.EXECUTED);
        CommandStatus newStatus = command.getParameters().getTaskGroupSuccess() ? CommandStatus.SUCCEEDED
                : CommandStatus.FAILED;
        log.info("Command '{}' id: '{}' child commands '{}' executions were completed, status '{}'",
                command.getActionType(), command.getCommandId(), childCmdIds, newStatus);
        if (!shouldExecuteEndMethod(command)) {
            logEndWillBeExecutedByParent(command, newStatus);
        }

        command.setCommandStatus(newStatus, false);
        command.persistCommand(command.getParameters().getParentCommand(), command.getCallback() != null);
    }

    protected abstract void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren);

    protected boolean shouldExecuteEndMethod(CommandBase<?> commandBase) {
        return !commandBase.isExecutedAsChildCommand()
                || shouldCommandEndOnAsyncOpEnd(commandBase);
    }


    private void endAction(CommandBase<?> commandBase, boolean succeeded) {
        if (shouldExecuteEndMethod(commandBase)) {
            commandBase.getReturnValue().setSucceeded(false);
            ActionReturnValue returnVal = commandBase.endAction();

            if (!returnVal.getSucceeded()) {
                if (shouldRepeatEndMethodsOnFail(returnVal)) {
                    throw new EngineException(EngineError.ENGINE, String.format("Command %1$s id: '%2$s' endAction() " +
                            "didn't complete successfully", commandBase.getActionType(), commandBase.getCommandId()));
                } else {
                    log.warn("Command '{}' id: '{}' end method execution failed, as the command isn't marked for " +
                            "endAction() retries silently ignoring", commandBase.getActionType(),
                            commandBase.getCommandId());
                }
            }

            finalizeCommand(commandBase, succeeded);
        }
    }

    @Override
    public void finalizeCommand(Guid cmdId, boolean succeeded) {
        finalizeCommand(getCommand(cmdId), succeeded);
    }

    private void finalizeCommand(CommandBase<?> command, boolean succeeded) {
        if (!command.isExecutedAsChildCommand()) {
            commandCoordinatorUtil.removeAllCommandsInHierarchy(command.getCommandId());
        }

        executionHandler.endJob(command.getExecutionContext(), succeeded);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        endAction(getCommand(cmdId), true);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        CommandBase<?> commandBase = getCommand(cmdId);
        // This should be removed as soon as infra bug will be fixed and failed execution will reach endWithFailure
        commandBase.getParameters().setTaskGroupSuccess(false);
        endAction(commandBase, false);
    }

    protected CommandBase<?> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }

    public void logEndWillBeExecutedByParent(CommandBase<?> command, CommandStatus status) {
        log.info(
                "Command '{}' id: '{}' Updating status to '{}', The command end method logic will be executed by one of its parent commands.",
                command.getActionType(),
                command.getCommandId(),
                status);
    }

    @Override
    public boolean pollOnExecutionFailed() {
        return true;
    }

    @Override
    public boolean shouldRepeatEndMethodsOnFail(Guid cmdId) {
        return shouldRepeatEndMethodsOnFail(commandCoordinatorUtil.getCommandEntity(cmdId).getReturnValue());
    }

    private boolean shouldRepeatEndMethodsOnFail(ActionReturnValue returnValue) {
        return returnValue.getEndActionTryAgain();
    }
}
