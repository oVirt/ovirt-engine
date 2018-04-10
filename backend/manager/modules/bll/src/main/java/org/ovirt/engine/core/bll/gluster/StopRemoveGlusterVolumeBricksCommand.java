package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;

/**
 * BLL command to stop remove brick asynchronous task started on a gluster volume
 */

@NonTransactiveCommandAttribute
public class StopRemoveGlusterVolumeBricksCommand extends GlusterAsyncCommandBase<GlusterVolumeRemoveBricksParameters> {
    @Inject
    private GlusterTaskUtils glusterTaskUtils;

    public StopRemoveGlusterVolumeBricksCommand(GlusterVolumeRemoveBricksParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE_BRICKS_STOP);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_BRICK);
        super.setActionMessageParameters();
    }

    @Override
    protected boolean validate() {
        GlusterVolumeEntity volume = getGlusterVolume();

        if (!super.validate()) {
            return false;
        }

        if (!validate(brickValidator.canStopOrCommitRemoveBrick(volume, getParameters().getBricks()))) {
            return false;
        }

        if (!glusterTaskUtils.isTaskOfType(volume, GlusterTaskType.REMOVE_BRICK)
                || !(glusterTaskUtils.isTaskStatus(volume, JobExecutionStatus.STARTED) || glusterTaskUtils.isTaskStatus(volume,
                        JobExecutionStatus.FINISHED))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_NOT_STARTED);
        }

        return true;
    }

    @Override
    protected StepEnum getStepType() {
        return StepEnum.REMOVING_BRICKS;
    }

    @Override
    protected void executeCommand() {
        GlusterVolumeEntity volume = getGlusterVolume();
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.StopRemoveGlusterVolumeBricks,
                        new GlusterVolumeRemoveBricksVDSParameters(getUpServer().getId(),
                                volume.getName(),
                                getParameters().getBricks()));

        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED, returnValue.getVdsError().getMessage());
            return;
        }

        GlusterVolumeTaskStatusEntity rebalanceStatusEntity =
                (GlusterVolumeTaskStatusEntity) returnValue.getReturnValue();
        endStepJobAborted(glusterTaskUtils.getSummaryMessage(rebalanceStatusEntity.getStatusSummary()));
        releaseVolumeLock();
        getReturnValue().setActionReturnValue(rebalanceStatusEntity);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP;
        } else {
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED;
        }
    }
}
