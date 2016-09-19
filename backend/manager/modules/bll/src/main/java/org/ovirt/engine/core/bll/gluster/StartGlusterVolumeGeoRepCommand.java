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

public class StartGlusterVolumeGeoRepCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionParameters> {

    public StartGlusterVolumeGeoRepCommand(GlusterVolumeGeoRepSessionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__START);
    }

    @Override
    protected void executeCommand() {
        GlusterGeoRepSession session = getGeoRepSession();
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.StartGlusterVolumeGeoRep,
                        new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                getGlusterVolumeName(),
                                session.getSlaveHostName(),
                                session.getSlaveVolumeName(),
                                session.getUserName(),
                                getParameters().isForce()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            session.setStatus(GeoRepSessionStatus.INITIALIZING);
            glusterGeoRepDao.updateSession(session);
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_GEO_REP_START_FAILED_EXCEPTION, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        if (getGeoRepSession().getStatus().equals(GeoRepSessionStatus.ACTIVE) && !getParameters().isForce()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_ALREADY_STARTED);
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if(getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_GEO_REP_START;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_GEO_REP_START_FAILED_EXCEPTION : errorType;
        }
    }
}
