package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

public class ResumeGeoRepSessionCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionParameters>{

    private GlusterVolumeGeoRepSessionParameters parameters;

    public ResumeGeoRepSessionCommand(GlusterVolumeGeoRepSessionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__RESUME);
    }

    @Override
    protected boolean validate() {
        parameters = getParameters();
        if (!super.validate()) {
            return false;
        }
        if (getGeoRepSession().getStatus().equals(GeoRepSessionStatus.ACTIVE)) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_ALREADY_RESUMED);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        GlusterGeoRepSession session = getGeoRepSession();
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.ResumeGeoRepSession,
                        new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                session.getMasterVolumeName(),
                                session.getSlaveHostName(),
                                session.getSlaveVolumeName(),
                                session.getUserName(),
                                parameters.isForce()));
        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_GEO_REP_RESUME_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if(getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_GEO_REP_RESUME;
        } else {
            return AuditLogType.GLUSTER_VOLUME_GEO_REP_RESUME_FAILED;
        }
    }
}
