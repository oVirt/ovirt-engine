package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.MaintenanceVdsParameters;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * {@code HostMaintenanceCallback} monitors the transition of host to maintenance mode. Once the host is moved to
 * maintenance mode, the callback will stop Gluster services on the host so that maintenance activities can be done on
 * the host.
 */
public class HostMaintenanceCallback extends CommandCallback {
    private String hostName;

    private static final Logger log = LoggerFactory.getLogger(HostMaintenanceCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        MaintenanceVdsCommand<MaintenanceVdsParameters> maintenanceCommand =
                CommandCoordinatorUtil.retrieveCommand(cmdId);
        evaluateMaintenanceHostCommandProgress(maintenanceCommand);
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
     * in order to stop gluster service
     *
     * @param maintenanceCommand
     *            The root command
     */
    private void evaluateMaintenanceHostCommandProgress(
            MaintenanceVdsCommand<MaintenanceVdsParameters> maintenanceCommand) {
        MaintenanceVdsParameters parameters = (MaintenanceVdsParameters) maintenanceCommand.getParameters();
        VdsDynamic host = DbFacade.getInstance().getVdsDynamicDao().get(parameters.getVdsId());

        switch (host.getStatus()) {

        // Wait till moving to maintenance ends
        case PreparingForMaintenance:
            break;

        // Stop Gluster processes
        case Maintenance:
            log.info("Host '{}' is on maintenance mode. Stoping all gluster services.",
                    getHostName(parameters.getVdsId()));
            stopGlusterServices(parameters.getVdsId());
            maintenanceCommand.setCommandStatus(CommandStatus.SUCCEEDED);
            break;

        // Any other status implies maintenance action failed, and the callback cannot proceed with stopping gluster's services
        default:
            if (isMaintenanceCommandExecuted(maintenanceCommand)) {
                log.info("Host '{}' failed to move to maintenance mode. Could not stop Gluster services.",
                        getHostName(parameters.getVdsId()));
                maintenanceCommand.setCommandStatus(CommandStatus.FAILED);
            }

            break;
        }
    }

    private boolean isMaintenanceCommandExecuted(MaintenanceVdsCommand<MaintenanceVdsParameters> maintenanceCommand) {
        CommandEntity maintenanceCmd = CommandCoordinatorUtil.getCommandEntity(maintenanceCommand.getCommandId());
        return maintenanceCmd != null && maintenanceCmd.isExecuted();
    }

    private void stopGlusterServices(Guid vdsId) {
        ResourceManager resourceManager = ResourceManager.getInstance();
        GlusterBrickDao glusterBrickDao = DbFacade.getInstance().getGlusterBrickDao();
        // Stop glusterd service first
        boolean succeeded = resourceManager.runVdsCommand(VDSCommandType.ManageGlusterService,
                new GlusterServiceVDSParameters(vdsId, Arrays.asList("glusterd"), "stop")).getSucceeded();
        if (succeeded) {
            // Stop other gluster related processes on the node
            succeeded = resourceManager.runVdsCommand(VDSCommandType.StopGlusterProcesses,
                    new VdsIdVDSCommandParametersBase(vdsId)).getSucceeded();
            // Mark the bricks as DOWN on this node
            if (succeeded) {
                List<GlusterBrickEntity> bricks = glusterBrickDao.getGlusterVolumeBricksByServerId(vdsId);
                bricks.stream().forEach(brick -> brick.setStatus(GlusterStatus.DOWN));
                glusterBrickDao.updateBrickStatuses(bricks);
            }
        }
        if (!succeeded) {
            log.error("Failed to stop gluster services while moving the host '{}' to maintenance", getHostName(vdsId));
        }
    }

    private String getHostName(Guid hostId) {
        if (hostName == null) {
            VdsStatic host = DbFacade.getInstance().getVdsStaticDao().get(hostId);
            hostName = host == null ? null : host.getName();
        }
        return hostName;
    }

}
