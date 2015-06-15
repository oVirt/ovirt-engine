package org.ovirt.engine.core.bll;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
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
                log.info("Waiting on child command id: '{}' type:'{}' of '{}' (id: '{}') to complete",
                        childCmdId,
                        child.getActionType(),
                        command.getActionType(),
                        cmdId);
                return;
            case FAILED:
            case FAILED_RESTARTED:
            case UNKNOWN:
                anyFailed = true;
                break;
            default:
                CommandEntity cmdEntity = CommandCoordinatorUtil.getCommandEntity(childCmdId);
                if (cmdEntity.isCallbackNotified() || !cmdEntity.isCallbackEnabled()) {
                    ++completedChildren;
                    break;
                } else {
                    // log.info("command '{}' id: '{}' is waiting for child command(s) '{}' to complete",
                    // command.getActionType(), cmdId, childCmdIds);
                    log.info("Waiting on child command id: '{}' type:'{}' of '{}' (id: '{}') to complete",
                            childCmdId,
                            child.getActionType(),
                            command.getActionType(),
                            cmdId);
                    return;
                }
            }
        }

        childCommandsExecutionEnded(command, anyFailed, childCmdIds, status, completedChildren);
    }

    protected abstract void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren);

    public void obtainChildCommands(CommandBase<?> commandBase, List<Guid> childCommands) {
        List<VdcActionParametersBase> parameters = new LinkedList<>();
        for (Guid id : childCommands) {
            CommandBase<?> command = CommandCoordinatorUtil.retrieveCommand(id);
            if (command.getParameters().getShouldBeEndedByParent()) {
                command.getParameters().setCommandType(command.getActionType());
                parameters.add(command.getParameters());
            }
        }

        commandBase.getParameters().setImagesParameters(parameters);
    }

    private void endAction(CommandBase<?> commandBase, List<Guid> childCmdIds) {
        if (commandBase.getParameters().getParentCommand() == VdcActionType.Unknown
                || !commandBase.getParameters().getShouldBeEndedByParent()) {

            obtainChildCommands(commandBase, childCmdIds);
            commandBase.endAction();

            if (commandBase.getParameters().getParentCommand() == VdcActionType.Unknown) {
                CommandCoordinatorUtil.removeAllCommandsInHierarchy(commandBase.getCommandId());
            }
        }
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        endAction(getCommand(cmdId), childCmdIds);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        CommandBase<?> commandBase = getCommand(cmdId);
        // This should be removed as soon as infra bug will be fixed and failed execution will reach endWithFailure
        commandBase.getParameters().setTaskGroupSuccess(false);
        endAction(commandBase, childCmdIds);
    }

    protected CommandBase<?> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
