package org.ovirt.engine.core.bll.hostdeploy;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpgradeHostParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code HostUpgradeCallback} monitors the transition of the host to maintenance status. Once the host is on
 * maintenance mode, the callback invokes the {@code UpgradeHostInternalCommand} for upgrading the host in async way.
 * The {@code UpgradeHostInternalCommand} is being monitored by this callback to its completion.
 */
@Typed(HostUpgradeCallback.class)
public class HostUpgradeCallback implements CommandCallback {

    private static final Logger log = LoggerFactory.getLogger(HostUpgradeCallback.class);
    private String hostName;
    private Guid maintenanceCmdId;
    private Guid hostUpgradeInternalCmdId;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    /**
     * The callback is being polling till the host move to maintenance or failed to do so.
     */
    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {

        CommandBase<?> rootCommand = commandCoordinatorUtil.retrieveCommand(cmdId);

        // If there are child commands and the host upgrade process was started, check the status of the host
        // upgrade command.
        if (childCommandsExist(childCmdIds) && evaluateHostUpgradeInternalCommandProgress(childCmdIds, rootCommand)) {
            return;
        }

        // if the host upgrade command was not started check the status of maintenance command
        if (Guid.isNullOrEmpty(getHostUpgradeInternalCmdId(childCmdIds))) {
            evaluateMaintenanceHostCommandProgress(childCmdIds, rootCommand);
        }
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        auditLogDirector.log(commandCoordinatorUtil.retrieveCommand(cmdId), AuditLogType.HOST_UPGRADE_FAILED);
        commandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        commandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
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
        VdsDynamic host = vdsDynamicDao.get(parameters.getVdsId());

        switch (host.getStatus()) {

        // Wait till moving to maintenance ends
        case PreparingForMaintenance:
            break;

        // Invoke the upgrade action
        case Maintenance:
            // if child command ids is empty the host is already in maintenance so no need to check for
            // MaintenanceNumberOfVdssCommand status. So upgrade can be started
            if (childCmdIds.isEmpty()) {
                logAndinvokeHostUpgrade(rootCommand, parameters);
                break;
            }
            switch (getMaintenanceCmdStatus(childCmdIds)) {
            case FAILED:
            case ENDED_WITH_FAILURE:
                log.info("Host '{}' is on maintenance mode. But not invoking Upgrade process because moving to " +
                        "maintenance completed with failure, please see logs for details.",
                        getHostName(parameters.getVdsId()));
                handleActionFailed(rootCommand, parameters);
                break;
            case SUCCEEDED:
            case ENDED_SUCCESSFULLY:
                logAndinvokeHostUpgrade(rootCommand, parameters);
                break;
            case UNKNOWN:
                log.info("Host '{}' is on maintenance mode. But not invoking Upgrade process because moving to " +
                                "maintenance command is in UNKNOWN status.",
                        getHostName(parameters.getVdsId()));
                break;
            default:
                log.info("Host '{}' is on maintenance mode. But not invoking Upgrade process because moving to " +
                                "maintenance command is in unhandled status '{}'.",
                        getHostName(parameters.getVdsId()),
                        host.getStatus());
                break;
            }
            break;

        // Any other status implies maintenance action failed, and the callback cannot proceed with the upgrade
        default:
            if (isMaintenanceCommandExecuted(childCmdIds) || hasMaintenanceCmdFailed(childCmdIds)) {
                handleActionFailed(rootCommand, parameters);
            }

            break;
        }
    }

    private void logAndinvokeHostUpgrade(CommandBase<?> rootCommand, UpgradeHostParameters parameters) {
        log.info("Host '{}' is on maintenance mode. Proceeding with Upgrade process.",
                getHostName(parameters.getVdsId()));
        invokeHostUpgrade(rootCommand, parameters);
    }

    private void handleActionFailed(CommandBase<?> rootCommand, UpgradeHostParameters parameters) {
        log.error("Host '{}' failed to move to maintenance mode. Upgrade process is terminated.",
                getHostName(parameters.getVdsId()));
        auditLogDirector.log(rootCommand, AuditLogType.VDS_MAINTENANCE_FAILED);
        rootCommand.setCommandStatus(CommandStatus.FAILED);
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
        return commandCoordinatorUtil.getCommandEntity(upgradeCmdId);
    }

    private void invokeHostUpgrade(CommandBase<?> command, UpgradeHostParameters parameters) {
        commandCoordinatorUtil.executeAsyncCommand(ActionType.UpgradeHostInternal,
                createUpgradeParameters(parameters),
                command.cloneContextAndDetachFromParent());
    }

    private ActionParametersBase createUpgradeParameters(UpgradeHostParameters parameters) {
        UpgradeHostParameters upgradeParams = new UpgradeHostParameters(parameters.getVdsId());
        upgradeParams.setSessionId(parameters.getSessionId());
        upgradeParams.setCorrelationId(parameters.getCorrelationId());
        upgradeParams.setInitialStatus(parameters.getInitialStatus());
        upgradeParams.setoVirtIsoFile(parameters.getoVirtIsoFile());
        upgradeParams.setParentCommand(ActionType.UpgradeHost);
        upgradeParams.setParentParameters(parameters);
        upgradeParams.setReboot(parameters.isReboot());
        upgradeParams.setTimeout(parameters.getTimeout());
        return upgradeParams;
    }

    private boolean isMaintenanceCommandExecuted(List<Guid> childCmdIds) {
        Guid maintenanceCommandId = getMaintenanceCmdId(childCmdIds);
        CommandEntity maintenanceCmd = commandCoordinatorUtil.getCommandEntity(maintenanceCommandId);
        return maintenanceCmd != null && maintenanceCmd.isExecuted();
    }

    private boolean hasMaintenanceCmdFailed(List<Guid> childCmdIds) {
        Guid maintenanceCommandId = getMaintenanceCmdId(childCmdIds);
        CommandEntity maintenanceCmd = commandCoordinatorUtil.getCommandEntity(maintenanceCommandId);
        return maintenanceCmd != null && maintenanceCmd.getCommandStatus() == CommandStatus.FAILED;
    }

    private CommandStatus getMaintenanceCmdStatus(List<Guid> childCmdIds) {
        Guid maintenanceCommandId = getMaintenanceCmdId(childCmdIds);
        CommandEntity maintenanceCmd = commandCoordinatorUtil.getCommandEntity(maintenanceCommandId);
        return maintenanceCmd == null ? CommandStatus.UNKNOWN : maintenanceCmd.getCommandStatus();
    }

    private String getHostName(Guid hostId) {
        if (hostName == null) {
            VdsStatic host = vdsStaticDao.get(hostId);
            hostName = host == null ? null : host.getName();
        }

        return hostName;
    }

    private Guid getHostUpgradeInternalCmdId(List<Guid> childCmdIds) {
        if (hostUpgradeInternalCmdId == null) {
            hostUpgradeInternalCmdId =
                    findChildCommandByActionType(ActionType.UpgradeHostInternal, childCmdIds);
        }

        return hostUpgradeInternalCmdId;
    }

    private Guid getMaintenanceCmdId(List<Guid> childCmdIds) {
        if (maintenanceCmdId == null) {
            maintenanceCmdId = findChildCommandByActionType(ActionType.MaintenanceNumberOfVdss, childCmdIds);
        }

        return maintenanceCmdId;
    }

    private Guid findChildCommandByActionType(ActionType commandType, List<Guid> childCmdIds) {
        for (Guid cmdId : childCmdIds) {
            CommandEntity commandEntity = commandCoordinatorUtil.getCommandEntity(cmdId);
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
