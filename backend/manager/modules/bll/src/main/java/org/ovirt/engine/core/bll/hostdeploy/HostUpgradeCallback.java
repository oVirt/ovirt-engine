package org.ovirt.engine.core.bll.hostdeploy;

import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code HostUpgradeCallback} monitors the transition of the host to maintenance status. Once the host is on
 * maintenance mode, the callback invokes the {@code UpgradeHostInternalCommand} for upgrading the host in async way.
 * The {@code UpgradeHostInternalCommand} is being monitored by this callback to its completion.
 */
public class HostUpgradeCallback extends CommandCallback {

    private static final Logger log = LoggerFactory.getLogger(HostUpgradeCallback.class);
    private String hostName;
    private Guid maintenanceCmdId;
    private Guid hostUpgradeInternalCmdId;

    /**
     * The callback is being polling till the host move to maintenance or failed to do so.
     */
    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {

        CommandBase<?> rootCommand = CommandCoordinatorUtil.retrieveCommand(cmdId);

        // Evaluate the host upgrade process. If wasn't started,
        if (childCommandsExist(childCmdIds) && evaluateHostUpgradeInternalCommandProgress(childCmdIds, rootCommand)) {
            return;
        }

        evaluateMaintenanceHostCommandProgress(childCmdIds, rootCommand);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    /**
     * Evaluates the host status in regards to maintenance status: The host must move to {@code VDSStatus.Maintenance}
     * in order to proceed with the upgrade process.
     *
     * @param childCmdIds
     *            child command IDs list to search if {@code MaintenanceNumberOfVdss} exists in it
     * @param rootCommand
     *            The root command
     */
    private void evaluateMaintenanceHostCommandProgress(List<Guid> childCmdIds, CommandBase<?> rootCommand) {
        UpgradeHostParameters parameters = (UpgradeHostParameters) rootCommand.getParameters();
        VdsDynamic host = DbFacade.getInstance().getVdsDynamicDao().get(parameters.getVdsId());

        switch (host.getStatus()) {

        // Wait till moving to maintenance ends
        case PreparingForMaintenance:
            break;

        // Invoke the upgrade action
        case Maintenance:
            log.info("Host '{}' is on maintenance mode. Proceeding with Upgrade process.",
                    getHostName(parameters.getVdsId()));
            invokeHostUpgrade(rootCommand, parameters);
            break;

        // Any other status implies maintenance action failed, and the callback cannot proceed with the upgrade
        default:
            if (isMaintenanceCommandExecuted(childCmdIds)) {
                log.info("Host '{}' failed to move to maintenance mode. Upgrade process is terminated.",
                        getHostName(parameters.getVdsId()));
                rootCommand.setCommandStatus(CommandStatus.FAILED);
            }

            break;
        }
    }

    /**
     * Evaluates the progress of the {@code UpgradeHostInternalCommand} and updates root command status accordingly
     *
     * @param childCmdIds
     *            The child command IDs list to search if {@code UpgradeHostInternalCommand} exists in it
     * @param rootCommand
     *            The root command
     * @return returns {@code true} if command execution has ended, else {@code false}
     */
    private boolean evaluateHostUpgradeInternalCommandProgress(List<Guid> childCmdIds, CommandBase<?> rootCommand) {
        CommandEntity upgradeCommand = getHostUpgradeInternalCommand(childCmdIds);
        if (upgradeCommand == null) {
            return false;
        }

        // upgrade command execution has started and its status should be examined
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

    private CommandEntity getHostUpgradeInternalCommand(List<Guid> childCmdIds) {
        Guid upgradeCmdId = getHostUpgradeInternalCmdId(childCmdIds);
        return CommandCoordinatorUtil.getCommandEntity(upgradeCmdId);
    }

    private void invokeHostUpgrade(CommandBase<?> command, UpgradeHostParameters parameters) {
        CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.UpgradeHostInternal,
                createUpgradeParameters(parameters),
                command.cloneContextAndDetachFromParent());
    }

    private VdcActionParametersBase createUpgradeParameters(UpgradeHostParameters parameters) {
        UpgradeHostParameters upgradeParams = new UpgradeHostParameters(parameters.getVdsId());
        upgradeParams.setSessionId(parameters.getSessionId());
        upgradeParams.setCorrelationId(parameters.getCorrelationId());
        upgradeParams.setInitialStatus(parameters.getInitialStatus());
        upgradeParams.setoVirtIsoFile(parameters.getoVirtIsoFile());
        upgradeParams.setParentCommand(VdcActionType.UpgradeHost);
        upgradeParams.setParentParameters(parameters);
        return upgradeParams;
    }

    private boolean isMaintenanceCommandExecuted(List<Guid> childCmdIds) {
        Guid maintenanceCommandId = getMaintenanceCmdId(childCmdIds);
        CommandEntity maintenanceCmd = CommandCoordinatorUtil.getCommandEntity(maintenanceCommandId);
        return maintenanceCmd != null && maintenanceCmd.isExecuted();
    }

    private String getHostName(Guid hostId) {
        if (hostName == null) {
            VdsStatic host = DbFacade.getInstance().getVdsStaticDao().get(hostId);
            hostName = host == null ? null : host.getName();
        }

        return hostName;
    }

    private Guid getHostUpgradeInternalCmdId(List<Guid> childCmdIds) {
        if (hostUpgradeInternalCmdId == null) {
            hostUpgradeInternalCmdId =
                    findChildCommandByActionType(VdcActionType.UpgradeHostInternal, childCmdIds);
        }

        return hostUpgradeInternalCmdId;
    }

    private Guid getMaintenanceCmdId(List<Guid> childCmdIds) {
        if (maintenanceCmdId == null) {
            maintenanceCmdId = findChildCommandByActionType(VdcActionType.MaintenanceNumberOfVdss, childCmdIds);
        }

        return maintenanceCmdId;
    }

    private Guid findChildCommandByActionType(VdcActionType commandType, List<Guid> childCmdIds) {
        for (Guid cmdId : childCmdIds) {
            CommandEntity commandEntity = CommandCoordinatorUtil.getCommandEntity(cmdId);
            if (commandEntity.getCommandType() == commandType) {
                return cmdId;
            }
        }

        return null;
    }

    private boolean childCommandsExist(List<Guid> childCmdIds) {
        return !childCmdIds.isEmpty();
    }
}
