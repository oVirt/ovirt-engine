package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.gluster.GlusterBrickValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.gluster.RemoveBrick;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;

/**
 * BLL command to Remove Bricks from Gluster volume
 */
@NonTransactiveCommandAttribute
public class GlusterVolumeRemoveBricksCommand extends GlusterVolumeCommandBase<GlusterVolumeRemoveBricksParameters> {
    private static final long serialVersionUID = 1465299601226267507L;
    private final List<GlusterBrickEntity> bricks = new ArrayList<>();

    @Inject
    private GlusterBrickValidator brickValidator;

    public GlusterVolumeRemoveBricksCommand(GlusterVolumeRemoveBricksParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveBrick.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_BRICK);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getGlusterVolume().getVolumeType().isDispersedType()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_REMOVE_BRICK_FROM_DISPERSE_VOLUME_NOT_SUPPORTED);
        }

        return validate(brickValidator.canRemoveBrick(getParameters().getBricks(),
                getGlusterVolume(),
                getParameters().getReplicaCount(),
                true));
    }

    @Override
    protected void executeCommand() {
        int replicaCount =
                (getGlusterVolume().getVolumeType() == GlusterVolumeType.REPLICATE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE)
                        ? getParameters().getReplicaCount()
                        : 0;
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.StartRemoveGlusterVolumeBricks,
                        new GlusterVolumeRemoveBricksVDSParameters(upServer.getId(),
                                getGlusterVolumeName(), getParameters().getBricks(), replicaCount, true));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            glusterDBUtils.removeBricksFromVolumeInDb(getGlusterVolume(), getParameters().getBricks(), replicaCount);
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED : errorType;
        }
    }

}
