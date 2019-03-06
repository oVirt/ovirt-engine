package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MaintenanceVdsParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
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
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
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
@Typed(HostMaintenanceCallback.class)
public class HostMaintenanceCallback implements CommandCallback {
    private String hostName;

    private static final Logger log = LoggerFactory.getLogger(HostMaintenanceCallback.class);

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private GlusterBrickDao glusterBrickDao;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    private GlusterUtil glusterUtil;

    @Inject
    private BackendInternal backend;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        MaintenanceVdsCommand<MaintenanceVdsParameters> maintenanceCommand =
                commandCoordinatorUtil.retrieveCommand(cmdId);
        evaluateMaintenanceHostCommandProgress(maintenanceCommand);
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
     * Evaluates the host status in regards to maintenance status: The host must move to {@code VDSStatus.Maintenance}
     * in order to stop gluster service
     *
     * @param maintenanceCommand
     *            The root command
     */
    private void evaluateMaintenanceHostCommandProgress(
            MaintenanceVdsCommand<MaintenanceVdsParameters> maintenanceCommand) {
        MaintenanceVdsParameters parameters = maintenanceCommand.getParameters();
        VdsDynamic host = vdsDynamicDao.get(parameters.getVdsId());

        switch (host.getStatus()) {

        // Wait till moving to maintenance ends
        case PreparingForMaintenance:
            break;

        // Stop Gluster processes
        case Maintenance:
            log.info("Host '{}' is on maintenance mode. Stoping all gluster services.",
                    getHostName(parameters.getVdsId()));
            stopGlusterServices(parameters.getVdsId());
            GlusterStatus isRunning = glusterUtil.isVDORunning(parameters.getVdsId());
            switch (isRunning) {
            case DOWN:
                log.info("VDO service is down in host : '{}' , skipping stopping of VDO service",
                        parameters.getVdsId());
                break;
            case UP:
                log.info("VDO service is up in host : '{}' ,  stopping VDO service", parameters.getVdsId());
                stopVDOService(parameters.getVdsId());
                break;
            case UNKNOWN:
                log.info("VDO service is not installed host : '{}' , ignoring stop VDO service",
                        parameters.getVdsId());
                break;
            }
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
        CommandEntity maintenanceCmd = commandCoordinatorUtil.getCommandEntity(maintenanceCommand.getCommandId());
        return maintenanceCmd != null && maintenanceCmd.isExecuted();
    }

    private void stopGlusterServices(Guid vdsId) {
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
                bricks.forEach(brick -> brick.setStatus(GlusterStatus.DOWN));
                glusterBrickDao.updateBrickStatuses(bricks);
            }
        }
        if (!succeeded) {
            log.error("Failed to stop gluster services while moving the host '{}' to maintenance", getHostName(vdsId));
            // activate the VDS, this will also restart the gluster services that failed to stop
            backend.runInternalAction(ActionType.ActivateVds, new VdsActionParameters(vdsId));
            throw new RuntimeException(String.format("Failed to stop gluster services while moving the host '%s' to maintenance",
                    getHostName(vdsId)));
        }
    }
    private void stopVDOService(Guid vdsId) {
        boolean succeeded = resourceManager.runVdsCommand(VDSCommandType.ManageGlusterService,
                new GlusterServiceVDSParameters(vdsId, Arrays.asList("vdo"), "stop")).getSucceeded();
        if (!succeeded) {
            log.error("Failed to stop VDO service while moving the host '{}' to maintenance", getHostName(vdsId));
            // activate the VDS, this will also restart the VDO services that failed to stop
            backend.runInternalAction(ActionType.ActivateVds, new VdsActionParameters(vdsId));
            throw new RuntimeException(String.format("Failed to stop VDO services while moving the host '%s' to maintenance",
                    getHostName(vdsId)));
        }
    }
    private String getHostName(Guid hostId) {
        if (hostName == null) {
            VdsStatic host = vdsStaticDao.get(hostId);
            hostName = host == null ? null : host.getName();
        }
        return hostName;
    }

}
