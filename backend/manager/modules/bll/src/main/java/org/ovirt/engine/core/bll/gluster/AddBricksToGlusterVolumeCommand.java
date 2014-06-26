package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeBricksActionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

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

        if (!validate(createVolumeValidator().isForceCreateVolumeAllowed(getVdsGroup().getcompatibility_version(),
                getParameters().isForce()))) {
            return false;
        }

        if (getGlusterVolume().getVolumeType().isReplicatedType()) {
            if (getParameters().getReplicaCount() > getGlusterVolume().getReplicaCount() + 1) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT_MORE_THAN_ONE);
            } else if (getParameters().getReplicaCount() < getGlusterVolume().getReplicaCount()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT);
            }
        }
        if (getGlusterVolume().getVolumeType().isStripedType()) {
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
        final List<GlusterBrickEntity> bricksList = getParameters().getBricks();

        // Add bricks in a single transaction
        if (bricksList != null && bricksList.size() > 0) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            addGlusterVolumeBricks(bricksList,
                                    getParameters().getReplicaCount(),
                                    getParameters().getStripeCount(),
                                    getParameters().isForce());
                            return null;
                        }
                    });
        }
    }

    private void addGlusterVolumeBricks(List<GlusterBrickEntity> bricksList,
            int replicaCount,
            int stripeCount,
            boolean force) {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.AddBricksToGlusterVolume,
                                new GlusterVolumeBricksActionVDSParameters(upServer.getId(),
                                        getGlusterVolumeName(),
                                        bricksList,
                                        replicaCount,
                                        stripeCount,
                                        upServer.getVdsGroupCompatibilityVersion(),
                                        force));

        setSucceeded(returnValue.getSucceeded());

        if (getSucceeded()) {
            addCustomValue(GlusterConstants.NO_OF_BRICKS, String.valueOf(bricksList.size()));
            addGlusterVolumeBricksInDb(bricksList, replicaCount, stripeCount);
            logAuditMessages(bricksList);
            getReturnValue().setActionReturnValue(getBrickIds(bricksList));
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    private void logAuditMessages(List<GlusterBrickEntity> bricks) {
        GlusterAuditLogUtil logUtil = GlusterAuditLogUtil.getInstance();
        for (final GlusterBrickEntity brick : bricks) {
            logUtil.logAuditMessage(null,
                    null,
                    null,
                    AuditLogType.GLUSTER_VOLUME_BRICK_ADDED,
                    new HashMap<String, String>() {
                        {
                            put(GlusterConstants.BRICK_PATH, brick.getBrickDirectory());
                            put(GlusterConstants.SERVER_NAME, brick.getServerName());
                            put(GlusterConstants.VOLUME_NAME, getGlusterVolumeName());
                        }
                    });
        }
    }

    private List<Guid> getBrickIds(List<GlusterBrickEntity> bricks) {
        List<Guid> brickIds = new ArrayList<Guid>();
        for (GlusterBrickEntity brick : bricks) {
            brickIds.add(brick.getId());
        }
        return brickIds;
    }

    private void addGlusterVolumeBricksInDb(List<GlusterBrickEntity> newBricks, int replicaCount, int stripeCount) {
        // Reorder the volume bricks
        GlusterVolumeEntity volume = getGlusterVolume();
        List<GlusterBrickEntity> volumeBricks = volume.getBricks();
        if (isReplicaCountIncreased(replicaCount) || isStripeCountIncreased(stripeCount)) {
            GlusterBrickEntity brick;
            int brick_num = 0;
            int count =
                    (isReplicaCountIncreased(replicaCount)) ? replicaCount : stripeCount;

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
        if (isReplicaCountIncreased(replicaCount)) {
            volume.setReplicaCount(replicaCount);
        }

        if (volume.getVolumeType() == GlusterVolumeType.REPLICATE
                && replicaCount < (volume.getBricks().size() + newBricks.size())) {
            volume.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
        }

        if (isStripeCountIncreased(stripeCount)) {
            volume.setStripeCount(stripeCount);
        }

        if (volume.getVolumeType() == GlusterVolumeType.STRIPE
                && stripeCount < (volume.getBricks().size() + newBricks.size())) {
            volume.setVolumeType(GlusterVolumeType.DISTRIBUTED_STRIPE);
        }
        //TODO: check for DISTRIBUTED_STRIPED_REPLICATE and STRIPED_REPLICATE

        getGlusterVolumeDao().updateGlusterVolume(volume);
    }

    private boolean isReplicaCountIncreased(int replicaCount) {
        return ((getGlusterVolume().getVolumeType() == GlusterVolumeType.REPLICATE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE)
                && replicaCount > getGlusterVolume().getReplicaCount());
    }

    private boolean isStripeCountIncreased(int stripeCount) {
        return ((getGlusterVolume().getVolumeType() == GlusterVolumeType.STRIPE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE)
                && stripeCount > getGlusterVolume().getStripeCount());
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
