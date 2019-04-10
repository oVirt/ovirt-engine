package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * BLL command to refresh gluster volume details
 */
@NonTransactiveCommandAttribute
public class RefreshGlusterVolumeDetailsCommand extends GlusterVolumeCommandBase<GlusterVolumeParameters> {

    public RefreshGlusterVolumeDetailsCommand(GlusterVolumeParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REFRESH);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean validate() {
        if(!super.validate()) {
            return false;
        }

        GlusterVolumeEntity glusterVolume = getGlusterVolume();
        if (!glusterVolume.isOnline()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED);
        }

        return true;
    }

    @Override
    protected void executeCommand() {

        glusterSyncJob.refreshVolumeDetails(upServer, getGlusterVolume());
        setSucceeded(true);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_DETAILS_REFRESH;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_DETAILS_REFRESH_FAILED : errorType;
        }
    }
}
