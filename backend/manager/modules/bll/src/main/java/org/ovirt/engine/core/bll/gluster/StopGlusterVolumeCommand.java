package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL command to stop a Gluster volume
 */
@NonTransactiveCommandAttribute
public class StopGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeActionParameters> {

    private static final long serialVersionUID = -7385050813049055828L;

    public StopGlusterVolumeCommand(GlusterVolumeActionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__STOP);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean canDoAction() {
        if(! super.canDoAction()) {
            return false;
        }

        if (!getGlusterVolume().isOnline()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ALREADY_STOPPED);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                                runVdsCommand(
                                        VDSCommandType.StopGlusterVolume,
                                        new GlusterVolumeActionVDSParameters(getUpServer().getId(),
                                                getGlusterVolumeName(), getParameters().isForceAction()));
        setSucceeded(returnValue.getSucceeded());
        if(getSucceeded()) {
            updateVolumeStatusInDb(getParameters().getVolumeId());
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_STOP_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_STOP;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_STOP_FAILED : errorType;
        }
    }

    private void updateVolumeStatusInDb(Guid volumeId) {
        getGlusterVolumeDao().updateVolumeStatus(volumeId, GlusterVolumeStatus.DOWN);
        updateBrickStatus(GlusterBrickStatus.DOWN);
    }

}
