package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeBricksActionVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AddBricksToGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeBricksActionParameters> {

    private static final long serialVersionUID = 1798863209150948961L;

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
        return updateBrickServerNames(getParameters().getBricks(), true);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.AddBricksToGlusterVolume,
                                new GlusterVolumeBricksActionVDSParameters(getUpServer().getId(),
                                        getGlusterVolumeName(),
                                        getParameters().getBricks(),
                                        getParameters().getReplicaCount(),
                                        getParameters().getStripeCount()));
        setSucceeded(returnValue.getSucceeded());

        if (getSucceeded()) {
            addGlusterVolumeBricksInDb(getParameters().getBricks(),
                    getParameters().getReplicaCount(),
                    getParameters().getStripeCount());
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    private void addGlusterVolumeBricksInDb(List<GlusterBrickEntity> bricks, int replicaCount, int stripeCount) {
        for (GlusterBrickEntity brick : bricks) {
            if (getGlusterVolume().getStatus() == GlusterVolumeStatus.UP) {
                brick.setStatus(GlusterBrickStatus.UP);
            } else {
                brick.setStatus(GlusterBrickStatus.DOWN);
            }
            getGlusterBrickDao().save(brick);
        }
        if (replicaCount != 0) {
            getGlusterVolumeDao().updateReplicaCount(bricks.get(0).getVolumeId(), replicaCount);
        }
        if (stripeCount != 0) {
            getGlusterVolumeDao().updateStripeCount(bricks.get(0).getVolumeId(), stripeCount);
        }
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
