package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * BLL command to discover and refresh geo-replication sessions in a volume
 */
@NonTransactiveCommandAttribute
public class RefreshGeoRepSessionsCommand<T extends GlusterVolumeParameters> extends GlusterVolumeCommandBase<T> {

    @Inject
    private GlusterGeoRepSyncJob glusterGeoRepSyncJob;

    public RefreshGeoRepSessionsCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
        setGlusterVolumeId(params.getVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REFRESH);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_GEOREP_SESSION);
        addValidationMessageVariable("volumeName", getGlusterVolumeName());
        addValidationMessageVariable("cluster", getClusterName());
    }

    @Override
    protected boolean validate() {
        if (getParameters().getVolumeId() == null || getGlusterVolume() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
            return false;
        }

        return super.validate();
    }

    @Override
    protected void executeCommand() {
        glusterGeoRepSyncJob.refreshGeoRepDataForVolume(getGlusterVolume());
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
