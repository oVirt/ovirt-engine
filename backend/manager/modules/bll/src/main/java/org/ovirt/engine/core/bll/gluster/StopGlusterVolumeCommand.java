package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;

/**
 * BLL command to stop a Gluster volume
 */
@NonTransactiveCommandAttribute
public class StopGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeActionParameters> {
    @Inject
    private GlusterTaskUtils glusterTaskUtils;

    public StopGlusterVolumeCommand(GlusterVolumeActionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__STOP);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
        addValidationMessageVariable("volumeName", getGlusterVolumeName());
        addValidationMessageVariable("cluster", getClusterName());
    }

    @Override
    protected boolean validate() {
        if(! super.validate()) {
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (!volume.isOnline()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ALREADY_STOPPED);
            addValidationMessageVariable("volumeName", volume.getName());
            return false;
        }

        if (glusterTaskUtils.isTaskOfType(volume, GlusterTaskType.REBALANCE)
                && glusterTaskUtils.isTaskStatus(volume, JobExecutionStatus.STARTED)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_CANNOT_STOP_REBALANCE_IN_PROGRESS);
        }

        if (glusterTaskUtils.isTaskOfType(volume, GlusterTaskType.REMOVE_BRICK)
                && glusterTaskUtils.isTaskStatus(volume, JobExecutionStatus.STARTED)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_CANNOT_STOP_REMOVE_BRICK_IN_PROGRESS);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                                runVdsCommand(
                                        VDSCommandType.StopGlusterVolume,
                                        new GlusterVolumeActionVDSParameters(upServer.getId(),
                                                getGlusterVolumeName(), getParameters().isForceAction()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            glusterDBUtils.updateVolumeStatus(getParameters().getVolumeId(), GlusterStatus.DOWN);
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_STOP_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_STOP;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_STOP_FAILED : errorType;
        }
    }
}
