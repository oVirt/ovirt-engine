package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL command to start a Gluster volume
 */
@NonTransactiveCommandAttribute
public class StartGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeActionParameters> {

    private static final long serialVersionUID = -7109431150062113267L;

    public StartGlusterVolumeCommand(GlusterVolumeActionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean canDoAction() {
        if(! super.canDoAction()) {
            return false;
        }

        if (getGlusterVolume().isOnline()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ALREADY_STARTED);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                                runVdsCommand(
                                        VDSCommandType.StartGlusterVolume,
                                        new GlusterVolumeActionVDSParameters(getUpServer().getId(),
                                                getGlusterVolumeName(), getParameters().isForceAction()));
        setSucceeded(returnValue.getSucceeded());
        if(getSucceeded()) {
            updateVolumeStatusInDb(getParameters().getVolumeId());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_START;
        } else {
            return AuditLogType.GLUSTER_VOLUME_START_FAILED;
        }
    }

    private void updateVolumeStatusInDb(Guid volumeId) {
        getGlusterVolumeDao().updateVolumeStatus(volumeId, GlusterVolumeStatus.UP);
    }
}
