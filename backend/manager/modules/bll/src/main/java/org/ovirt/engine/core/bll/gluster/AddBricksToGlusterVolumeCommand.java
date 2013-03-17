package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeBricksActionVDSParameters;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public class AddBricksToGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeBricksActionParameters> {

    public AddBricksToGlusterVolumeCommand(GlusterVolumeBricksActionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
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
        if (getGlusterVolume().getVolumeType() == GlusterVolumeType.REPLICATE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
            if (getParameters().getReplicaCount() > getGlusterVolume().getReplicaCount() + 1) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT_MORE_THAN_ONE);
            } else if (getParameters().getReplicaCount() < getGlusterVolume().getReplicaCount()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT);
            }
        } else if (getGlusterVolume().getVolumeType() == GlusterVolumeType.STRIPE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE) {
            if (getParameters().getStripeCount() > getGlusterVolume().getStripeCount() + 1) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_STRIPE_COUNT_MORE_THAN_ONE);
            } else if (getParameters().getStripeCount() < getGlusterVolume().getStripeCount()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_STRIPE_COUNT);
            }
        }

        return updateBrickServerNames(getParameters().getBricks(), true)
                && validateDuplicateBricks(getParameters().getBricks());
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.AddBricksToGlusterVolume,
                                new GlusterVolumeBricksActionVDSParameters(upServer.getId(),
                                        getGlusterVolumeName(),
                                        getParameters().getBricks(),
                                        getParameters().getReplicaCount(),
                                        getParameters().getStripeCount()));
        setSucceeded(returnValue.getSucceeded());

        if (getSucceeded()) {
            addGlusterVolumeBricksInDb(getParameters().getBricks());
            getReturnValue().setActionReturnValue(getBrickIds(getParameters().getBricks()));
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    private List<Guid> getBrickIds(List<GlusterBrickEntity> bricks) {
        List<Guid> brickIds = new ArrayList<Guid>();
        for (GlusterBrickEntity brick : bricks) {
            brickIds.add(brick.getId());
        }
        return brickIds;
    }

    private void addGlusterVolumeBricksInDb(List<GlusterBrickEntity> newBricks) {
        // Reorder the volume bricks
        GlusterVolumeEntity volume = getGlusterVolume();
        List<GlusterBrickEntity> volumeBricks = volume.getBricks();
        if (isReplicaCountIncreased() || isStripeCountIncreased()) {
            GlusterBrickEntity brick;
            int brick_num = 0;
            int count =
                    (isReplicaCountIncreased()) ? getParameters().getReplicaCount() : getParameters().getStripeCount();

            // Updating existing brick order
            for (int i = 0; i < volumeBricks.size(); i++) {
                if (((i + 1) % count) == 0) {
                    brick_num++;
                }
                brick = volumeBricks.get(i);
                brick.setBrickOrder(brick_num);
                brick_num++;

                getGlusterBrickDao().updateBrickOrder(brick.getId(), brick.getBrickOrder());
            }
            // Adding new bricks
            for (int i = 0; i < newBricks.size(); i++) {
                brick = newBricks.get(i);
                brick.setBrickOrder((i + 1) * count - 1);
                brick.setStatus(getBrickStatus());
                getGlusterBrickDao().save(brick);
            }

        } else {
            // No change in the replica/stripe count
            int brickCount = volumeBricks.get(volumeBricks.size() - 1).getBrickOrder();

            for (GlusterBrickEntity brick : newBricks) {
                brick.setBrickOrder(++brickCount);
                brick.setStatus(getBrickStatus());
                getGlusterBrickDao().save(brick);
            }
        }

        // Update the volume replica/stripe count
        if (isReplicaCountIncreased()) {
            volume.setReplicaCount(getParameters().getReplicaCount());
        }

        if (volume.getVolumeType() == GlusterVolumeType.REPLICATE
                && getParameters().getReplicaCount() < (volume.getBricks().size() + getParameters().getBricks()
                        .size())) {
            volume.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
        }

        if (isStripeCountIncreased()) {
            volume.setStripeCount(getParameters().getStripeCount());
        }

        if (volume.getVolumeType() == GlusterVolumeType.STRIPE
                && getParameters().getStripeCount() < (volume.getBricks().size() + getParameters().getBricks()
                        .size())) {
            volume.setVolumeType(GlusterVolumeType.DISTRIBUTED_STRIPE);
        }

        getGlusterVolumeDao().updateGlusterVolume(volume);
    }

    private boolean isReplicaCountIncreased() {
        if ((getGlusterVolume().getVolumeType() == GlusterVolumeType.REPLICATE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE)
                && getParameters().getReplicaCount() > getGlusterVolume().getReplicaCount()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isStripeCountIncreased() {
        if ((getGlusterVolume().getVolumeType() == GlusterVolumeType.STRIPE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE)
                && getParameters().getStripeCount() > getGlusterVolume().getStripeCount()) {
            return true;
        } else {
            return false;
        }
    }

    private GlusterStatus getBrickStatus() {
        return getGlusterVolume().getStatus();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_ADD_BRICK;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED : errorType;
        }
    }
}
