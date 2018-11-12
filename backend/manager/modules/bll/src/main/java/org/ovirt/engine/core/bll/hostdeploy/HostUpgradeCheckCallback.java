package org.ovirt.engine.core.bll.hostdeploy;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code HostUpgradeCallback} monitors the invocation of  the {@code CheckForHostUpgradeInternalCommand} for checking
 * if there is an upgrade available for the host in async way.
 * The {@code CheckForHostUpgradeInternalCommand} is being monitored by this callback to its completion.
 */
@Typed(HostUpgradeCheckCallback.class)
public class HostUpgradeCheckCallback implements CommandCallback {

    private Guid checkForHostUpgradeInternalCmdId;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    /**
     * The callback is being polling till the host move to maintenance or failed to do so.
     */
    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {

        CommandBase<?> rootCommand = commandCoordinatorUtil.retrieveCommand(cmdId);

        if (childCommandsExist(childCmdIds)) {
            evaluateHostUpgradeCheckInternalCommandProgress(childCmdIds, rootCommand);
        }

    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        commandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        commandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    /**
     * Evaluates the progress of the {@code HostUpgradeCheckInternalCommand} and updates root command status
     * accordingly
     *
     * @param childCmdIds
     *            The child command IDs list to search if {@code HostUpgradeCheckInternalCommand} exists in it
     * @param rootCommand
     *            The root command
     * @return returns {@code true} if command execution has ended, else {@code false}
     */
    private boolean evaluateHostUpgradeCheckInternalCommandProgress(List<Guid> childCmdIds,
                                                                    CommandBase<?> rootCommand) {
        CommandEntity upgradeCommand = getHostUpgradeCheckInternalCommand(childCmdIds);
        if (upgradeCommand == null) {
            return false;
        }

        // upgrade check command execution has started and its status should be examined
        switch (upgradeCommand.getCommandStatus()) {
        case ACTIVE:
        case NOT_STARTED:
            return false;
        case FAILED:
        case EXECUTION_FAILED:
        case ENDED_WITH_FAILURE:
        case UNKNOWN:
            rootCommand.setCommandStatus(CommandStatus.FAILED);
            return true;

        case SUCCEEDED:
        case ENDED_SUCCESSFULLY:
            rootCommand.setCommandStatus(CommandStatus.SUCCEEDED);
            return true;
        }

        return true;
    }

    private CommandEntity getHostUpgradeCheckInternalCommand(List<Guid> childCmdIds) {
        Guid upgradeCmdId = getHostUpgradeInternalCmdId(childCmdIds);
        return commandCoordinatorUtil.getCommandEntity(upgradeCmdId);
    }

    private Guid getHostUpgradeInternalCmdId(List<Guid> childCmdIds) {
        if (checkForHostUpgradeInternalCmdId == null) {
            checkForHostUpgradeInternalCmdId =
                    findChildCommandByActionType(ActionType.HostUpgradeCheckInternal, childCmdIds);
        }

        return checkForHostUpgradeInternalCmdId;
    }

    private Guid findChildCommandByActionType(ActionType commandType, List<Guid> childCmdIds) {
        return childCmdIds.stream()
                .filter(cmdId -> commandCoordinatorUtil.getCommandEntity(cmdId).getCommandType() == commandType)
                .findFirst().orElse(null);
    }

    private boolean childCommandsExist(List<Guid> childCmdIds) {
        return !childCmdIds.isEmpty();
    }
}
