package org.ovirt.engine.core.bll.storage.dr;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

@Typed(GlusterStorageGeoRepSyncCallback.class)
public class GlusterStorageGeoRepSyncCallback implements CommandCallback {

    @Inject
    private GlusterGeoRepDao geoRepDao;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        CommandBase<?> rootCommand = commandCoordinatorUtil.retrieveCommand(cmdId);
        evaluateGeoRepSessionStatus(rootCommand);
    }

    private void evaluateGeoRepSessionStatus(CommandBase<?> rootCommand) {
        GlusterVolumeGeoRepSessionParameters parameters =
                (GlusterVolumeGeoRepSessionParameters) rootCommand.getParameters();
        GlusterGeoRepSession session = geoRepDao.getById(parameters.getGeoRepSessionId());

        if (session == null) {
            rootCommand.setCommandStatus(CommandStatus.FAILED);
            return;
        }
        session.setSessionDetails(
                (ArrayList<GlusterGeoRepSessionDetails>) geoRepDao.getGeoRepSessionDetails(session.getId()));
        switch (session.getStatus()) {
        case ACTIVE:
            // check if checkpoint is completed
            if (session.isCheckPointCompleted()) {
                // stop the geo-rep session
                stopGeoRepSessionCommand(rootCommand, session);
                rootCommand.setCommandStatus(CommandStatus.SUCCEEDED);
            }
            break;
        case FAULTY:
            rootCommand.setCommandStatus(CommandStatus.FAILED);
        default:
            break;
        }
    }

    private void stopGeoRepSessionCommand(CommandBase<?> command, GlusterGeoRepSession session) {
        commandCoordinatorUtil.executeAsyncCommand(ActionType.StopGeoRepSession,
                new GlusterVolumeGeoRepSessionParameters(session.getMasterVolumeId(), session.getId()),
                command.cloneContextAndDetachFromParent());
    }
}
