package org.ovirt.engine.core.bll.validator.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * Helps to validate the details of the bricks.
 *
 */
public class GlusterBrickValidator {

    /**
     * Checks that all brick ids passed are valid, also updating the bricks with server name and brick directory using
     * the brick objects obtained from the volume.
     *
     * @param bricks
     *            The bricks to validate
     * @return true if all bricks have valid ids, else false
     */
    public ValidationResult validateBricks(List<GlusterBrickEntity> bricks, GlusterVolumeEntity volumeEntity, int replicaCount) {
        if (bricks.isEmpty()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
        }
        if (volumeEntity.getBricks().size() == 1 ||
                volumeEntity.getBricks().size() <= bricks.size()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REMOVE_ALL_BRICKS_FROM_VOLUME);
        }
        if (volumeEntity.getVolumeType().isReplicatedType()) {
            if (replicaCount < volumeEntity.getReplicaCount() - 1) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_MORE_THAN_ONE);
            } else if (replicaCount > volumeEntity.getReplicaCount()) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT);
            }
        }

        for (GlusterBrickEntity brick : bricks) {
            if (brick.getId(false) == null && brick.getQualifiedName() == null) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            }

            GlusterBrickEntity brickFromVolume = volumeEntity.getBrickWithId(brick.getId());
            if(brickFromVolume == null) {
                brickFromVolume = volumeEntity.getBrickWithQualifiedName(brick.getQualifiedName());
            }

            if (brickFromVolume == null) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_BRICK_INVALID);
            } else {
                // Fill required details from volume data
                brick.setServerName(brickFromVolume.getServerName());
                brick.setBrickDirectory(brickFromVolume.getBrickDirectory());
            }

        }

        return ValidationResult.VALID;
    }
}
