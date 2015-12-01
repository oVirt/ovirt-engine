package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.gluster.GlusterBrickValidator;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class GlusterBrickValidatorTest {
    private GlusterBrickValidator brickValidator = new GlusterBrickValidator();
    private final Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");

    private GlusterVolumeEntity getDistributedVolume(Guid volumeId, int brickCount) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(volumeId);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volumeEntity.setBricks(getBricks(volumeId, brickCount));
        return volumeEntity;
    }

    private GlusterVolumeEntity getDistributedReplicatedVolume(Guid volumeId, int brickCount, int replicaCount) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(volumeId);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
        volumeEntity.setBricks(getBricks(volumeId, brickCount));
        volumeEntity.setReplicaCount(replicaCount);
        return volumeEntity;
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int max) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        for (Integer i = 0; i < max; i++) {
            GlusterBrickEntity brick = new GlusterBrickEntity();
            brick.setVolumeId(volumeId);
            brick.setServerName("server1");
            brick.setStatus(GlusterStatus.UP);
            brick.setBrickDirectory("/tmp/s" + i.toString());
            bricks.add(brick);
            brick.setId(Guid.newGuid());
        }
        return bricks;
    }

    @Test
    public void canRebalanceOnDistributedVolume() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 5);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void canRebalanceOnDistributedVolumeWithBricksDown() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 5);

        // One Brick Down
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertFalse(validationResult.isValid());

        // Two Bricks Down
        volumeEntity.getBricks().get(4).setStatus(GlusterStatus.DOWN);
        validationResult = brickValidator.canRebalance(volumeEntity);
        assertFalse(validationResult.isValid());

        // One Brick Down
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.UP);
        validationResult = brickValidator.canRebalance(volumeEntity);
        assertFalse(validationResult.isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolume() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolumeWithFewBrickDown() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(1).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(2).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertTrue(validationResult.isValid());

        volumeEntity.getBricks().get(4).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(5).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(6).setStatus(GlusterStatus.DOWN);
        validationResult = brickValidator.canRebalance(volumeEntity);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolumeWithOneReplicaPairDown() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(1).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(2).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertFalse(validationResult.isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolumeWithTwoReplicaPairDown() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(2).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(4).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(5).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(6).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(7).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(8).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertFalse(validationResult.isValid());
    }

    @Test
    public void canRemoveBrickEmptyList() {
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(Collections.<GlusterBrickEntity> emptyList(),
                        getDistributedVolume(volumeId1, 1),
                        3,
                        false);
        assertFalse(validationResult.isValid());
        assertTrue(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED == validationResult.getMessage());
    }

    @Test
    public void canRemoveLastBrick() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 1);
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(volumeEntity.getBricks(), volumeEntity, 1, false);
        assertFalse(validationResult.isValid());
        assertTrue(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REMOVE_ALL_BRICKS_FROM_VOLUME == validationResult.getMessage());
    }

    @Test
    public void canRemoveAllBricksFromSubVolume() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 9, 3);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.add(volumeEntity.getBricks().get(6));
        bricksToRemove.add(volumeEntity.getBricks().get(7));
        bricksToRemove.add(volumeEntity.getBricks().get(8));

        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 3, false);
        assertTrue(validationResult.isValid());

    }

    @Test
    public void canRemoveBrickIfSomeBricksAreDown() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.add(volumeEntity.getBricks().get(1));
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 1, false);
        assertFalse(validationResult.isValid());
        assertTrue(EngineMessage.ACTION_TYPE_FAILED_ONE_OR_MORE_BRICKS_ARE_DOWN == validationResult.getMessage());

    }

    @Test
    public void canRemoveWithOutforceAndReduceReplicaCount() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.add(volumeEntity.getBricks().get(0));
        bricksToRemove.add(volumeEntity.getBricks().get(4));
        bricksToRemove.add(volumeEntity.getBricks().get(8));

        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 3, false);
        assertFalse(validationResult.isValid());
        assertTrue(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_WITH_DATA_MIGRATION == validationResult.getMessage());

    }

    @Test
    public void canRemoveBrickReduceReplicaMoreThanOne() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.add(volumeEntity.getBricks().get(0));
        bricksToRemove.add(volumeEntity.getBricks().get(4));
        bricksToRemove.add(volumeEntity.getBricks().get(8));
        bricksToRemove.add(volumeEntity.getBricks().get(1));
        bricksToRemove.add(volumeEntity.getBricks().get(5));
        bricksToRemove.add(volumeEntity.getBricks().get(9));

        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 2, false);
        assertFalse(validationResult.isValid());
        assertTrue(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_MORE_THAN_ONE == validationResult.getMessage());
    }

    @Test
    public void canRemoveIncreaseReplica() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.add(volumeEntity.getBricks().get(0));

        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 5, false);
        assertFalse(validationResult.isValid());
        assertTrue(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT == validationResult.getMessage());
    }

    @Test
    public void canRemoveNonExistentBrick() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.addAll(getBricks(volumeEntity.getId(), 1));
        bricksToRemove.get(0).setBrickDirectory("NewServer:/NewExport");
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 1, false);
        assertFalse(validationResult.isValid());
        assertTrue(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_BRICK_INVALID == validationResult.getMessage());
    }

    @Test
    public void canRemoveBrickUpdatesBrickDetalis() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.addAll(getBricks(volumeEntity.getId(), 1));
        bricksToRemove.get(0).setId(volumeEntity.getBricks().get(2).getId());
        bricksToRemove.get(0).setServerName(null);
        bricksToRemove.get(0).setBrickDirectory(null);

        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 1, false);
        assertTrue(validationResult.isValid());
        assertNotNull(bricksToRemove.get(0).getServerName());
        assertNotNull(bricksToRemove.get(0).getBrickDirectory());

    }
}
