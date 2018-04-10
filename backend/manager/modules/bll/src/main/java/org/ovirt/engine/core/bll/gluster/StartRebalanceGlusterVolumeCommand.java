package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRebalanceVDSParameters;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

/**
 * BLL command to Rebalance Gluster volume This command starts an asynchronous gluster task to start rebalance of
 * volume. This may be a long running operation and so the returned task id is used to update the status of the step and
 * the corresponding job.
 */

@NonTransactiveCommandAttribute
public class StartRebalanceGlusterVolumeCommand extends GlusterAsyncCommandBase<GlusterVolumeRebalanceParameters> {

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    public StartRebalanceGlusterVolumeCommand(GlusterVolumeRebalanceParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REBALANCE_START);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
        super.setActionMessageParameters();
    }

    @Override
    protected boolean validate() {
        GlusterVolumeEntity glusterVolume = getGlusterVolume();
        if (!super.validate()) {
            return false;
        }

        boolean isVolumeDistributed = glusterVolume.getVolumeType().isDistributedType();
        if (!isVolumeDistributed) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_NOT_DISTRIBUTED);
        } else if (glusterVolume.getBricks().size() == 1) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_DISTRIBUTED_AND_HAS_SINGLE_BRICK);
        }

        return validate(brickValidator.canRebalance(glusterVolume));
    }

    @Override
    protected StepEnum getStepType() {
        return StepEnum.REBALANCING_VOLUME;
    }

    @Override
    protected void executeCommand() {
        startSubStep();
        VDSReturnValue taskReturn = runVdsCommand(VDSCommandType.StartRebalanceGlusterVolume, new GlusterVolumeRebalanceVDSParameters(upServer.getId(),
                getGlusterVolumeName(),
                getParameters().isFixLayoutOnly(),
                getParameters().isForceAction()));
        setSucceeded(taskReturn.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED, taskReturn.getVdsError().getMessage());
            return;
        }
        GlusterAsyncTask glusterTask = (GlusterAsyncTask) taskReturn.getReturnValue();
        handleTaskReturn(glusterTask);
        updateVolumeWithTaskID(glusterTask);
        getReturnValue().setActionReturnValue(glusterTask);

    }

    private void updateVolumeWithTaskID(GlusterAsyncTask rebalanceAsyncTask) {
        glusterVolumeDao.updateVolumeTask(getGlusterVolumeId(), rebalanceAsyncTask.getTaskId());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_START;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED : errorType;
        }
    }

    @Override
    protected void freeLock() {
        // We have to keep the lock acquired on gluster volume if start rebalance was success, otherwsie we should
        // release all the locks.
        if (!getSucceeded()) {
            super.freeLock();
        }
    }

}
