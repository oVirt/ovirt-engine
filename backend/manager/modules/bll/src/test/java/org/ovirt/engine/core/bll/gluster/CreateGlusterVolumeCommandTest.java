package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreateGlusterVolumeCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    GlusterBrickDao brickDao;

    @Mock
    VdsStaticDao vdsStaticDao;

    @Mock
    ClusterDao clusterDao;

    @Mock
    NetworkDao networkDao;

    @Mock
    InterfaceDao interfaceDao;

    private static final String serverName = "myhost";

    private static final Guid clusterId = new Guid("c0dd8ca3-95dd-44ad-a88a-440a6e3d8106");

    private static final Guid serverId = new Guid("d7f10a21-bbf2-4ffd-aab6-4da0b3b2ccec");

    @Spy
    @InjectMocks
    private CreateGlusterVolumeCommand cmd = createTestCommand(getVolume(2, false));

    private CreateGlusterVolumeCommand createTestCommand(GlusterVolumeEntity volumeEntity) {
        CreateGlusterVolumeParameters parameters = new CreateGlusterVolumeParameters(volumeEntity);
        return new CreateGlusterVolumeCommand(parameters, null);
    }

    private void setVolume(GlusterVolumeEntity volumeEntity) {
        cmd.getParameters().setVolume(volumeEntity);
        cmd.setGlusterVolumeId(volumeEntity.getId());
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(clusterId);
        vds.setStatus(status);
        return vds;
    }

    private VdsStatic getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setClusterId(clusterId);
        vds.setHostName(serverName);
        return vds;
    }

    private Cluster getCluster(boolean glusterEnabled, Version clusterVersion) {
        Cluster cluster = new Cluster();
        cluster.setId(clusterId);
        cluster.setCompatibilityVersion(clusterVersion);
        cluster.setVirtService(false);
        cluster.setGlusterService(glusterEnabled);
        return cluster;
    }

    @BeforeEach
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getVdsStatic()).when(vdsStaticDao).get(serverId);
        doReturn(getCluster(true, Version.v4_3)).when(clusterDao).get(any());
    }

    private GlusterVolumeEntity getVolume(int brickCount, boolean withDuplicateBricks){
        return getVolume(brickCount, withDuplicateBricks, GlusterVolumeType.DISTRIBUTE, 0, false);
    }

    private GlusterVolumeEntity getVolume(int brickCount, boolean withDuplicateBricks, GlusterVolumeType volumeType, int replicaCount, boolean isArbiter) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(Guid.newGuid());
        volumeEntity.setClusterId(clusterId);
        volumeEntity.setName("vol1");
        volumeEntity.setVolumeType(volumeType);
        volumeEntity.setBricks(getBricks(volumeEntity.getId(), brickCount, withDuplicateBricks));
        volumeEntity.setReplicaCount(replicaCount);
        volumeEntity.setIsArbiter(isArbiter);
        return volumeEntity;
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int max, boolean withDuplicates) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick = null;
        for (Integer i = 0; i < max; i++) {
            brick = new GlusterBrickEntity();
            brick.setVolumeId(volumeId);
            brick.setServerId(serverId);
            brick.setServerName(serverName);
            brick.setBrickDirectory("/tmp/s" + i.toString());
            brick.setStatus(GlusterStatus.UP);
            bricks.add(brick);
        }

        if (max > 0 && withDuplicates) {
            bricks.add(brick);
        }
        return bricks;
    }

    @Test
    public void validateSucceeds() {
        assertTrue(cmd.validate());
    }

    @Test
    public void validateSucceedsWithArbiter() {
        setVolume(getVolume(3, false, GlusterVolumeType.REPLICATE, 3, true));
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsWithArbiterAndInvalidReplica() {
        setVolume(getVolume(2, false, GlusterVolumeType.REPLICATE, 2, true));
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_GLUSTER_ARBITER_VOLUME_SHOULD_BE_REPLICA_3_VOLUME);
    }

    @Test
    public void validateFailsWithClusterDoesNotSupportGluster() {
        doReturn(getCluster(false, Version.v4_3)).when(clusterDao).get(any());
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_CLUSTER_DOES_NOT_SUPPORT_GLUSTER);
    }

    @Test
    public void validateFailsWithDuplicateVolumeName() {
        doReturn(getVolume(2, false)).when(volumeDao).getByName(clusterId, "vol1");
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_NAME_ALREADY_EXISTS);
    }

    @Test
    public void validateFailsWithEmptyBricks() {
        setVolume(getVolume(0, false));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
    }

    @Test
    public void validateFailsWithDuplicateBricks() {
        setVolume(getVolume(2, true));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_BRICKS);
    }
}
