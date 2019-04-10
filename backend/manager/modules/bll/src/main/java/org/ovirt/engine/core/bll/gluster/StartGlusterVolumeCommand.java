package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

/**
 * BLL command to start a Gluster volume
 */
@NonTransactiveCommandAttribute
public class StartGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeActionParameters> {

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    public StartGlusterVolumeCommand(GlusterVolumeActionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__START);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean validate() {
        if(! super.validate()) {
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (volume.isOnline() && !getParameters().isForceAction()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ALREADY_STARTED);
            addValidationMessageVariable("volumeName", volume.getName());
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                                runVdsCommand(
                                        VDSCommandType.StartGlusterVolume,
                                        new GlusterVolumeActionVDSParameters(upServer.getId(),
                                                getGlusterVolumeName(), getParameters().isForceAction()));
        setSucceeded(returnValue.getSucceeded());
        if(getSucceeded()) {
            glusterDBUtils.updateVolumeStatus(getParameters().getVolumeId(), GlusterStatus.UP);
            /* Refresh volume details once the volume is started.
             * A specific requirement for this was user might create a volume for the sake of using it for geo-replication.
             * However, for suggesting volumes eligible for session creation, the size information of the volume is very important.
             * Having the user to wait for the sync job to sync the volume detail might not be appropriate.
             */
            glusterSyncJob.refreshVolumeDetails(upServer, glusterVolumeDao.getById(getParameters().getVolumeId()));
            /* GlusterSyncJob.getInstance().refreshVolumeDetails(upServer, getGlusterVolume());
             * will not suffice bcoz, getGlusterVolume fetches new volume only if its not yet been fetched from db and hence, refreshVolumeDetails figures out
             * that the info about volume-bricks are stale and hence attempts a update and correspondingly raises events for brick state change.
             * But here in the previous step we changed the volumes state(To GlusterStatus.UP) due to a successful execution of start command.
             * Hence fetch the volume afresh after the state change and use it to refresh volume details.
             */
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_START_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_START;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_START_FAILED : errorType;
        }
    }
}
