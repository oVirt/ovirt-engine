package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.validation.group.gluster.RemoveBrick;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL command to Remove Bricks from Gluster volume
 */
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public class GlusterVolumeRemoveBricksCommand extends GlusterVolumeCommandBase<GlusterVolumeRemoveBricksParameters> {
    private static final long serialVersionUID = 1465299601226267507L;
    private List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();

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
        if (getParameters().getBricks() == null || getParameters().getBricks().size() == 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            return false;
        }
        if (getGlusterVolume().getBricks().size() == 1 ||
                getGlusterVolume().getBricks().size() <= getParameters().getBricks().size()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REMOVE_ALL_BRICKS_FROM_VOLUME);
            return false;
        }
        if (getGlusterVolume().getVolumeType() == GlusterVolumeType.REPLICATE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
            if (getParameters().getReplicaCount() < getGlusterVolume().getReplicaCount() - 1) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_MORE_THAN_ONE);
                return false;
            } else if (getParameters().getReplicaCount() > getGlusterVolume().getReplicaCount()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT);
                return false;
            }
        }
        return validateBricks(getParameters().getBricks());
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
                        VDSCommandType.GlusterVolumeRemoveBricks,
                        new GlusterVolumeRemoveBricksVDSParameters(upServer.getId(),
                                getGlusterVolumeName(), bricks, replicaCount));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            removeBricksFromVolumeInDb(bricks);
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    /**
     * Checks that all brick ids passed are valid, also populating the class level bricks list with populated brick
     * objects obtained from the volume.
     *
     * @param bricks The bricks to validate
     * @return true if all bricks have valid ids, else false
     */
    private boolean validateBricks(List<GlusterBrickEntity> bricks) {
        GlusterVolumeEntity volume = getGlusterVolume();
        for (GlusterBrickEntity brick : bricks) {
            if (brick.getServerName() != null && brick.getBrickDirectory() != null) {
                // brick already contains required info.
                this.bricks.add(brick);
                continue;
            }

            if (brick.getId(false) == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
                return false;
            }

            GlusterBrickEntity brickFromVolume = volume.getBrickWithId(brick.getId());
            if (brickFromVolume == null) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_BRICK_INVALID);
                return false;
            } else {
                this.bricks.add(brickFromVolume);
            }
        }

        return true;
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
        for (GlusterBrickEntity brick : brickList) {
            getGlusterBrickDao().removeBrick(brick.getId());
        }

        // Update volume type and replica/stripe count
        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE
                && volume.getReplicaCount() == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.REPLICATE);
        }
        if (volume.getVolumeType() == GlusterVolumeType.REPLICATE
                || volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
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
    }
}
