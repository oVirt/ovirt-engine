package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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

    private GlusterVolumeEntity getDistributedReplacatedVolume(Guid volumeId, int brickCount, int replicaCount) {
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
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        for (Integer i = 0; i < max; i++) {
            GlusterBrickEntity brick1 = new GlusterBrickEntity();
            brick1.setVolumeId(volumeId);
            brick1.setServerName("server1");
            brick1.setStatus(GlusterStatus.UP);
            brick1.setBrickDirectory("/tmp/s" + i.toString());
            bricks.add(brick1);
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
        GlusterVolumeEntity volumeEntity = getDistributedReplacatedVolume(volumeId1, 12, 4);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolumeWithFewBrickDown() {
        GlusterVolumeEntity volumeEntity = getDistributedReplacatedVolume(volumeId1, 12, 4);
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
        GlusterVolumeEntity volumeEntity = getDistributedReplacatedVolume(volumeId1, 12, 4);
        volumeEntity.getBricks().get(0).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(1).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(2).setStatus(GlusterStatus.DOWN);
        volumeEntity.getBricks().get(3).setStatus(GlusterStatus.DOWN);
        ValidationResult validationResult = brickValidator.canRebalance(volumeEntity);
        assertFalse(validationResult.isValid());
    }

    @Test
    public void canRebalanceOnDistributeReplicateVolumeWithTwoReplicaPairDown() {
        GlusterVolumeEntity volumeEntity = getDistributedReplacatedVolume(volumeId1, 12, 4);
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

}
