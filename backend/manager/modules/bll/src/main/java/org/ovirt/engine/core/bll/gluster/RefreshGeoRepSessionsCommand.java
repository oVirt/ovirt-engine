package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;


/**
 * BLL command to discover and refresh geo-replication sessions in a volume
 */
@NonTransactiveCommandAttribute
public class RefreshGeoRepSessionsCommand<T extends GlusterVolumeParameters> extends GlusterCommandBase<T> {

    public RefreshGeoRepSessionsCommand(T params) {
        super(params);
        setGlusterVolumeId(params.getVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REFRESH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_GEOREP_SESSION);
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getVolumeId() == null || getGlusterVolume() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
            return false;
        }

        return super.canDoAction();
    }

    protected GlusterGeoRepSyncJob getSyncJobInstance() {
        return GlusterGeoRepSyncJob.getInstance();
    }

    @Override
    protected void executeCommand() {
        getSyncJobInstance().refreshGeoRepDataForVolume(getGlusterVolume());
        setSucceeded(true);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_GEOREP_SESSION_REFRESH;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_GEOREP_SESSION_REFRESH_FAILED : errorType;
        }
    }
}
