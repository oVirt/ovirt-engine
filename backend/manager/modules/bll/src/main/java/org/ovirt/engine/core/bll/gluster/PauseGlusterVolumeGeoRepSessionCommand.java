package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

public class PauseGlusterVolumeGeoRepSessionCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionParameters> {

    public PauseGlusterVolumeGeoRepSessionCommand(GlusterVolumeGeoRepSessionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__PAUSE);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }
        if (getGeoRepSession().getStatus() == GeoRepSessionStatus.PASSIVE) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GEOREP_SESSION_ALREADY_PAUSED);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        GlusterGeoRepSession session = getGeoRepSession();
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.PauseGlusterVolumeGeoRepSession,
                        new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                session.getMasterVolumeName(),
                                session.getSlaveHostName(),
                                session.getSlaveVolumeName(),
                                getParameters().isForce()));
        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_GEO_REP_PAUSE_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_GEO_REP_PAUSE;
        } else {
            return AuditLogType.GLUSTER_VOLUME_GEO_REP_PAUSE_FAILED;
        }
    }
}
