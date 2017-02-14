package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;

/**
 * BLL command to Stop the active Rebalancing on a Gluster volume.
 */

@NonTransactiveCommandAttribute
public class StopRebalanceGlusterVolumeCommand extends GlusterAsyncCommandBase<GlusterVolumeRebalanceParameters> {
    @Inject
    private GlusterTaskUtils glusterTaskUtils;

    public StopRebalanceGlusterVolumeCommand(GlusterVolumeRebalanceParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REBALANCE_STOP);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
        super.setActionMessageParameters();
    }

    @Override
    protected boolean validate() {
        GlusterVolumeEntity glusterVolume = getGlusterVolume();
        if (!super.validate()) {
            return false;
        }

        if (!glusterTaskUtils.isTaskOfType(glusterVolume, GlusterTaskType.REBALANCE)
                || !glusterTaskUtils.isTaskStatus(glusterVolume, JobExecutionStatus.STARTED)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_REBALANCE_NOT_STARTED);
        }
        return true;
    }

    @Override
    protected StepEnum getStepType() {
        return StepEnum.REBALANCING_VOLUME;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnaValue =
                runVdsCommand(VDSCommandType.StopRebalanceGlusterVolume,
                        new GlusterVolumeVDSParameters(upServer.getId(),
                                getGlusterVolumeName()));
        if (!vdsReturnaValue.getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED, vdsReturnaValue.getVdsError()
                    .getMessage());
            setSucceeded(false);
            return;
        }

        GlusterVolumeTaskStatusEntity rebalanceStatusEntity =
                (GlusterVolumeTaskStatusEntity) vdsReturnaValue.getReturnValue();
        JobExecutionStatus stepStatus = rebalanceStatusEntity.getStatusSummary().getStatus();
        if (stepStatus != null) {
            endStepJob(stepStatus,
                    getStepMessageMap(stepStatus,
                            glusterTaskUtils.getSummaryMessage(rebalanceStatusEntity.getStatusSummary())),
                    glusterTaskUtils.isTaskSuccess(stepStatus));

        } else {
            endStepJob(JobExecutionStatus.ABORTED, getStepMessageMap(JobExecutionStatus.ABORTED, null), false);
        }
        releaseVolumeLock();
        setSucceeded(vdsReturnaValue.getSucceeded());
        getReturnValue().setActionReturnValue(rebalanceStatusEntity);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED : errorType;
        }
    }
}
