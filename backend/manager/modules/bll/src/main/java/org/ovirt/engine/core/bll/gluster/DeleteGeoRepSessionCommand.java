package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

/**
 * BLL command to stop a geo-replication session
 */
@NonTransactiveCommandAttribute
public class DeleteGeoRepSessionCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionParameters> {

    public DeleteGeoRepSessionCommand(GlusterVolumeGeoRepSessionParameters params) {
        super(params);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(false);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_GEOREP_SESSION);
        addCanDoActionMessageVariable("volumeName", getGlusterVolumeName());
        addCanDoActionMessageVariable("vdsGroup", getVdsGroupName());
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.DeleteGlusterVolumeGeoRepSession,
                        new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                getGeoRepSession().getMasterVolumeName(), getGeoRepSession().getSlaveHostName(),
                                getGeoRepSession().getSlaveVolumeName(), getGeoRepSession().getUserName()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            getGlusterGeoRepDao().remove(getGeoRepSession().getId());
        } else {
            handleVdsError(AuditLogType.GEOREP_SESSION_DELETE_FAILED, returnValue.getVdsError().getMessage());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GEOREP_SESSION_DELETED;
        } else {
            return errorType == null ? AuditLogType.GEOREP_SESSION_DELETE_FAILED : errorType;
        }
    }

}
