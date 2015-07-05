package org.ovirt.engine.core.bll.gluster;

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
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

/**
 * BLL command to start a Gluster volume
 */
@NonTransactiveCommandAttribute
public class StartGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeActionParameters> {

    public StartGlusterVolumeCommand(GlusterVolumeActionParameters params) {
        super(params);
    }

    public StartGlusterVolumeCommand(GlusterVolumeActionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__START);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean canDoAction() {
        if(! super.canDoAction()) {
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (volume.isOnline()) {
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ALREADY_STARTED);
            addCanDoActionMessageVariable("volumeName", volume.getName());
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
            GlusterDBUtils.getInstance().updateVolumeStatus(getParameters().getVolumeId(), GlusterStatus.UP);
            GlusterSyncJob.getInstance().refreshVolumeDetails(upServer, getGlusterVolume());
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
