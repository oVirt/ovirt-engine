package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * BLL command to delete a Gluster volume
 */
@NonTransactiveCommandAttribute
public class DeleteGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeParameters> {

    public DeleteGlusterVolumeCommand(GlusterVolumeParameters params) {
        super(params);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (volume.isOnline()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_UP);
            addCanDoActionMessageVariable("volumeName", volume.getName());
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.DeleteGlusterVolume,
                        new GlusterVolumeVDSParameters(upServer.getId(),
                                getGlusterVolumeName()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            updateVolumeStatusInDb(getParameters().getVolumeId());
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_DELETE_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_DELETE;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_DELETE_FAILED : errorType;
        }
    }

    private void updateVolumeStatusInDb(Guid volumeId) {
        getGlusterVolumeDao().remove(volumeId);
    }
}
