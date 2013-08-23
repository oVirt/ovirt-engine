package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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

    public StopRebalanceGlusterVolumeCommand(GlusterVolumeRebalanceParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REBALANCE_STOP);
        super.setActionMessageParameters();
    }

    @Override
    protected boolean canDoAction() {
        GlusterVolumeEntity glusterVolume = getGlusterVolume();
        if (!super.canDoAction()) {
            return false;
        }

        if (glusterVolume.getAsyncTask() == null
                || glusterVolume.getAsyncTask().getType() != GlusterTaskType.REBALANCE_VOLUME
                || glusterVolume.getAsyncTask().getStatus() != JobExecutionStatus.STARTED) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_REBALANCE_NOT_STARTED);
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
        setSucceeded(vdsReturnaValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED, vdsReturnaValue.getVdsError()
                    .getMessage());
            return;
        }

        endStepJob();
        clearVolumeTaskAndReleaseLock();
        getReturnValue().setActionReturnValue(vdsReturnaValue);
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
