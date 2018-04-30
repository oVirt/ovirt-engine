package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
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
        assertThat(validationResult, isValid());
    }

    @Test
    public void canRebalanceOnDistributedVolumeWithBricksDown() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 5);

        // One Brick Down
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertThat(validationResult, not(isValid()));

        // Two Bricks Down
        volumeEntity.getBricks().get(4).setStatus(GlusterStatus.DOWN);
        validationResult = brickValidator.canRebalance(volumeEntity);
        assertThat(validationResult, not(isValid()));

        // One Brick Down
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.UP);
        validationResult = brickValidator.canRebalance(volumeEntity);
        assertThat(validationResult, not(isValid()));
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolume() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertThat(validationResult, isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolumeWithFewBrickDown() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(1).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(2).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertThat(validationResult, isValid());

        volumeEntity.getBricks().get(4).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(5).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(6).setStatus(GlusterStatus.DOWN);
        validationResult = brickValidator.canRebalance(volumeEntity);
        assertThat(validationResult, isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolumeWithOneReplicaPairDown() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(1).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(2).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertThat(validationResult, not(isValid()));
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
        assertThat(validationResult, not(isValid()));
    }

    @Test
    public void canRemoveBrickEmptyList() {
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(Collections.emptyList(),
                        getDistributedVolume(volumeId1, 1),
                        3,
                        false);
        assertThat(validationResult, failsWith(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED));
    }

    @Test
    public void canRemoveLastBrick() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 1);
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(volumeEntity.getBricks(), volumeEntity, 1, false);
        assertThat(validationResult, failsWith(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REMOVE_ALL_BRICKS_FROM_VOLUME));
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
        assertThat(validationResult, isValid());
    }

    @Test
    public void canRemoveBrickIfSomeBricksAreDown() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.add(volumeEntity.getBricks().get(1));
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 1, false);
        assertThat(validationResult, failsWith(EngineMessage.ACTION_TYPE_FAILED_ONE_OR_MORE_BRICKS_ARE_DOWN));
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
        assertThat(validationResult,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_WITH_DATA_MIGRATION));
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
        assertThat(validationResult,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT_MORE_THAN_ONE));
    }

    @Test
    public void canRemoveIncreaseReplica() {
        GlusterVolumeEntity volumeEntity = getDistributedReplicatedVolume(volumeId1, 12, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.add(volumeEntity.getBricks().get(0));

        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 5, false);
        assertThat(validationResult, failsWith(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT));
    }

    @Test
    public void canRemoveNonExistentBrick() {
        GlusterVolumeEntity volumeEntity = getDistributedVolume(volumeId1, 4);
        List<GlusterBrickEntity> bricksToRemove = new ArrayList<>();
        bricksToRemove.addAll(getBricks(volumeEntity.getId(), 1));
        bricksToRemove.get(0).setBrickDirectory("NewServer:/NewExport");
        ValidationResult validationResult =
                brickValidator.canRemoveBrick(bricksToRemove, volumeEntity, 1, false);
        assertThat(validationResult, failsWith(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_BRICK_INVALID));
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
        assertThat(validationResult, isValid());
        assertThat(bricksToRemove.get(0).getServerName(), notNullValue());
        assertThat(bricksToRemove.get(0).getBrickDirectory(), notNullValue());
    }
}
