package org.ovirt.engine.core.bll.gluster;

import java.util.Map;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

/**
 * BLL command to commit bricks removal asynchronous task started on a gluster volume
 */

@NonTransactiveCommandAttribute
public class CommitRemoveGlusterVolumeBricksCommand extends GlusterAsyncCommandBase<GlusterVolumeRemoveBricksParameters> {

    public CommitRemoveGlusterVolumeBricksCommand(GlusterVolumeRemoveBricksParameters params,
            CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE_BRICKS_COMMIT);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_BRICK);
        super.setActionMessageParameters();
    }

    @Override
    protected boolean validate() {
        GlusterVolumeEntity volume = getGlusterVolume();

        if (!super.validate()) {
            return false;
        }

        if (!validate(getBrickValidator().canStopOrCommitRemoveBrick(volume, getParameters().getBricks()))) {
            return false;
        }

        if (!getGlusterTaskUtils().isTaskOfType(volume, GlusterTaskType.REMOVE_BRICK)
                || !getGlusterTaskUtils().isTaskStatus(volume, JobExecutionStatus.FINISHED)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_NOT_FINISHED);
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
                runVdsCommand(VDSCommandType.CommitRemoveGlusterVolumeBricks,
                        new GlusterVolumeRemoveBricksVDSParameters(getUpServer().getId(),
                                volume.getName(),
                                getParameters().getBricks()));
        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT_FAILED, returnValue.getVdsError()
                    .getMessage());
            return;
        }

        addCustomValue(GlusterConstants.NO_OF_BRICKS, String.valueOf(getParameters().getBricks().size()));
        endStepJobCommitted();
        getDbUtils().removeBricksFromVolumeInDb(volume,
                getParameters().getBricks(),
                getParameters().getReplicaCount());
        getGlusterVolumeDao().updateVolumeTask(volume.getId(), null);
        releaseVolumeLock();
        getReturnValue().setActionReturnValue(returnValue.getReturnValue());
    }

    protected void endStepJobCommitted() {
        endStepJob(JobExecutionStatus.FINISHED, getStepMessageMap(JobExecutionStatus.FINISHED, null), true);
    }

    @Override
    protected Map<String, String> getStepMessageMap(JobExecutionStatus status, String jobInfo) {
        Map<String, String> stepMessageMap = super.getStepMessageMap(status, jobInfo);
        stepMessageMap.put(GlusterConstants.JOB_STATUS, "COMMITTED");
        return stepMessageMap;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT;
        } else {
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT_FAILED;
        }
    }

    @Override
    public BackendInternal getBackend() {
        return super.getBackend();
    }

    public GlusterDBUtils getDbUtils() {
        return GlusterDBUtils.getInstance();
    }
}
