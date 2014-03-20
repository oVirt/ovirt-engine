package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;


/**
 * BLL command to refresh gluster volume details
 */
@NonTransactiveCommandAttribute
public class RefreshGlusterVolumeDetailsCommand extends GlusterVolumeCommandBase<GlusterVolumeParameters> {

    public RefreshGlusterVolumeDetailsCommand(GlusterVolumeParameters params) {
        super(params);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REFRESH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean canDoAction() {
        if(!super.canDoAction()) {
            return false;
        }

        GlusterVolumeEntity glusterVolume = getGlusterVolume();
        if (!glusterVolume.isOnline()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED);
        }

        return true;
    }

    protected GlusterSyncJob getSyncJobInstance() {
        return GlusterSyncJob.getInstance();
    }

    @Override
    protected void executeCommand() {

        getSyncJobInstance().refreshVolumeDetails(upServer, getGlusterVolume());
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
