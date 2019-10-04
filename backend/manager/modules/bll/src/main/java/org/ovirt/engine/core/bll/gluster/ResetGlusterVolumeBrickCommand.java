package org.ovirt.engine.core.bll.gluster;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeResetBrickActionParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.ResetGlusterVolumeBrickActionVDSParameters;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

/**
 * BLL command to Reset Gluster volume brick
 */
@NonTransactiveCommandAttribute
public class ResetGlusterVolumeBrickCommand extends GlusterVolumeCommandBase<GlusterVolumeResetBrickActionParameters> {

    @Inject
    private GlusterBrickDao glusterBrickDao;

    public ResetGlusterVolumeBrickCommand(GlusterVolumeResetBrickActionParameters params,
            CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_BRICK);
        addValidationMessage(EngineMessage.VAR__ACTION__RESET_GLUSTER_BRICK);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (!getGlusterVolume().getVolumeType().isReplicatedType()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_NOT_SUPPORTED_FOR_VOLUME_TYPE);
            return false;
        }

        if (!getGlusterVolume().isOnline()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_DOWN);
            return false;
        }

        if (getGlusterVolume().getAsyncTask() != null && (getGlusterVolume().getAsyncTask().getStatus() == JobExecutionStatus.STARTED
                || (getGlusterVolume().getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK
                && getGlusterVolume().getAsyncTask().getStatus() == JobExecutionStatus.FINISHED))) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_HAS_RUNNING_TASKS);
            return false;
        }

        if (getParameters().getExistingBrick() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            return false;
        }

        if (!isValidVolumeBrick(getParameters().getExistingBrick())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NOT_A_GLUSTER_VOLUME_BRICK);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.ResetGlusterVolumeBrick,
                        new ResetGlusterVolumeBrickActionVDSParameters(upServer.getId(),
                                getGlusterVolumeName(),
                                getParameters().getExistingBrick().getQualifiedName()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            glusterBrickDao.updateBrickStatus(getParameters().getExistingBrick().getId(), GlusterStatus.UP);
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_RESET_BRICK_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_BRICK_RESETED;
        } else {
            return AuditLogType.GLUSTER_VOLUME_RESET_BRICK_FAILED;
        }
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.BRICK, getParameters().getExistingBrick().getQualifiedName());
        return super.getCustomValues();
    }

    private boolean isValidVolumeBrick(GlusterBrickEntity volumeBrick) {
        for (GlusterBrickEntity brick : getGlusterVolume().getBricks()) {
            if (brick.getQualifiedName().equals(volumeBrick.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

}
