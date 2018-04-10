package org.ovirt.engine.core.bll.validator.gluster;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

/**
 * Helps to validate the details of the bricks.
 *
 */
@Singleton
public class GlusterBrickValidator {
    @Inject
    private GlusterBrickDao glusterBrickDao;

    /**
     * Checks that all brick ids passed are valid, also updating the bricks with server name and brick directory using
     * the brick objects obtained from the volume.
     *
     * @param bricks
     *            The bricks to validate
     * @return true if all bricks have valid ids, else false
     */
    public ValidationResult canRemoveBrick(List<GlusterBrickEntity> bricks,
            GlusterVolumeEntity volumeEntity,
            int replicaCount, boolean forceRemove) {
        if (replicaCount == 0) {
            replicaCount = volumeEntity.getReplicaCount();
        }
        if (bricks.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
        }
        if (volumeEntity.getBricks().size() == 1 ||
                volumeEntity.getBricks().size() <= bricks.size()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REMOVE_ALL_BRICKS_FROM_VOLUME);
        }
        if (volumeEntity.getVolumeType().isReplicatedType()) {
            if (replicaCount == volumeEntity.getReplicaCount() - 1 && !forceRemove) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_WITH_DATA_MIGRATION);
            } else if (replicaCount < volumeEntity.getReplicaCount() - 1) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_MORE_THAN_ONE);
            } else if (replicaCount > volumeEntity.getReplicaCount()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT);
            }
        }

        for (GlusterBrickEntity brick : bricks) {
            if (brick.getId(false) == null && brick.getQualifiedName() == null) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            }

            GlusterBrickEntity brickFromVolume = volumeEntity.getBrickWithId(brick.getId());
            if (brickFromVolume == null) {
                brickFromVolume = volumeEntity.getBrickWithQualifiedName(brick.getQualifiedName());
            }

            if (brickFromVolume == null) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_BRICK_INVALID);
            } else {
                // Fill required details from volume data
                brick.setId(brickFromVolume.getId());
                brick.setServerName(brickFromVolume.getServerName());
                brick.setBrickDirectory(brickFromVolume.getBrickDirectory());
            }

        }

        if (!forceRemove) {
            return canRebalance(volumeEntity);
        } else {
            return ValidationResult.VALID;
        }
    }

    public ValidationResult canRebalance(GlusterVolumeEntity volumeEntity) {
        int replicaCount = 1;
        List<GlusterBrickEntity> bricks = volumeEntity.getBricks();
        if (volumeEntity.getVolumeType().isReplicatedType()) {
            replicaCount = volumeEntity.getReplicaCount();
        }

        int brickIndex = 0;
        int replicaIndex = 0;
        while (brickIndex < bricks.size()) {
            for (replicaIndex = 0; replicaIndex < replicaCount; replicaIndex++) {
                if (bricks.get(brickIndex + replicaIndex).isOnline()) {
                    brickIndex = brickIndex + replicaCount;
                    break;
                }
            }
            if (replicaIndex == replicaCount) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_ONE_OR_MORE_BRICKS_ARE_DOWN);
            }

        }

        return ValidationResult.VALID;
    }

    public ValidationResult canStopOrCommitRemoveBrick(GlusterVolumeEntity volumeEntity,
            List<GlusterBrickEntity> paramBricks) {
        GlusterAsyncTask asyncTask = volumeEntity.getAsyncTask();
        if (asyncTask == null || asyncTask.getType() != GlusterTaskType.REMOVE_BRICK) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID_TASK_TYPE);
        }

        if (paramBricks.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
        }

        List<GlusterBrickEntity> bricksForTask = glusterBrickDao.getGlusterVolumeBricksByTaskId(asyncTask.getTaskId());


        if (paramBricks.size() != bricksForTask.size() || !areBricksInTheList(volumeEntity, paramBricks, bricksForTask)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_REMOVE_BRICKS_PARAMS_INVALID,
                    String.format("$validBricks [%s]", getValidBrickNames(bricksForTask)));
        }

        return ValidationResult.VALID;
    }

    private boolean areBricksInTheList(GlusterVolumeEntity volumeEntity,
            final List<GlusterBrickEntity> bricks,
            final List<GlusterBrickEntity> searchBricks) {
        boolean found = false;

        for (GlusterBrickEntity paramBrick : bricks) {
            GlusterBrickEntity paramBrickFromVolume = volumeEntity.getBrickWithId(paramBrick.getId());
            if (paramBrickFromVolume == null) {
                paramBrickFromVolume = volumeEntity.getBrickWithQualifiedName(paramBrick.getQualifiedName());
            }

            // reset the flag for next round
            found = false;
            for (GlusterBrickEntity brick : searchBricks) {
                if (paramBrickFromVolume == null || paramBrickFromVolume.getId() == null) {
                    return false;
                }

                // If parameter brick directory matches with any brick no need to continue further to check
                if (paramBrickFromVolume.getId().equals(brick.getId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return found;
    }

    private String getValidBrickNames(List<GlusterBrickEntity> bricksForTask) {
        if (bricksForTask.size() == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (GlusterBrickEntity brick : bricksForTask) {
            builder.append(brick.getQualifiedName());
            builder.append(", ");
        }

        return builder.substring(0, builder.length() - 2).toString();
    }
}
