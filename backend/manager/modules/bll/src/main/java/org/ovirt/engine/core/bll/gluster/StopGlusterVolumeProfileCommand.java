package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;

/**
 * BLL command to Stop Gluster Volume Profile
 */
@NonTransactiveCommandAttribute
public class StopGlusterVolumeProfileCommand extends GlusterVolumeCommandBase<GlusterVolumeParameters> {

    public StopGlusterVolumeProfileCommand(GlusterVolumeParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__STOP_PROFILE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.StopGlusterVolumeProfile,
                        new GlusterVolumeVDSParameters(upServer.getId(), getGlusterVolumeName()));
        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_PROFILE_STOP_FAILED, returnValue.getVdsError().getMessage());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_PROFILE_STOP;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_PROFILE_STOP_FAILED : errorType;
        }
    }
}
