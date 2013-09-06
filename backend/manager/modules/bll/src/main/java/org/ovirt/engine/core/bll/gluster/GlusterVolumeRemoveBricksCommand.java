package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.validator.gluster.GlusterBrickValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.gluster.RemoveBrick;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;

/**
 * BLL command to Remove Bricks from Gluster volume
 */
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public class GlusterVolumeRemoveBricksCommand extends GlusterVolumeCommandBase<GlusterVolumeRemoveBricksParameters> {
    private static final long serialVersionUID = 1465299601226267507L;
    private final List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();

    public GlusterVolumeRemoveBricksCommand(GlusterVolumeRemoveBricksParameters params) {
        super(params);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(RemoveBrick.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_BRICK);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        GlusterBrickValidator brickValidator = new GlusterBrickValidator();
        return validate(brickValidator.validateBricks(getParameters().getBricks(), getGlusterVolume(), getParameters().getReplicaCount()));
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
            removeBricksFromVolumeInDb(getParameters().getBricks());
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

    private void removeBricksFromVolumeInDb(List<GlusterBrickEntity> brickList) {
        GlusterVolumeEntity volume = getGlusterVolume();

        getGlusterBrickDao().removeAllInBatch(brickList);

        // Update volume type and replica/stripe count
        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE
                && volume.getReplicaCount() == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.REPLICATE);
        }
        if (volume.getVolumeType().isReplicatedType()) {
            int replicaCount =
                    (getParameters().getReplicaCount() == 0)
                            ? volume.getReplicaCount()
                            : getParameters().getReplicaCount();
            volume.setReplicaCount(replicaCount);
            getGlusterVolumeDao().updateGlusterVolume(volume);
        }

        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE
                && volume.getStripeCount() == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.STRIPE);
            getGlusterVolumeDao().updateGlusterVolume(volume);
        }

        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPED_REPLICATE
                && (volume.getStripeCount() * volume.getReplicaCount()) == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.STRIPED_REPLICATE);
            getGlusterVolumeDao().updateGlusterVolume(volume);
        }
    }
}
