package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeResetBrickActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ResetGlusterVolumeBrickCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    VdsStaticDao vdsStaticDao;

    @Mock
    NetworkDao networkDao;

    @Mock
    InterfaceDao interfaceDao;

    private static final String serverName = "myhost";
    private final Guid clusterId = new Guid("c0dd8ca3-95dd-44ad-a88a-440a6e3d8106");
    private final Guid serverId = new Guid("d7f10a21-bbf2-4ffd-aab6-4da0b3b2ccec");
    private final Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private final Guid volumeId2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private final Guid volumeId3 = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid volumeId4 = new Guid("000000000000-0000-0000-0000-00000004");

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private ResetGlusterVolumeBrickCommand cmd =
            new ResetGlusterVolumeBrickCommand(new GlusterVolumeResetBrickActionParameters(), null);

    @BeforeEach
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getVolume(volumeId1, GlusterVolumeType.DISTRIBUTED_REPLICATE, 0)).when(volumeDao).getById(volumeId1);
        doReturn(getVolume(volumeId2, GlusterVolumeType.DISTRIBUTED_REPLICATE, 0)).when(volumeDao).getById(volumeId2);
        doReturn(getVolume(volumeId3, GlusterVolumeType.REPLICATE, 2)).when(volumeDao).getById(volumeId3);
        doReturn(getVolume(volumeId4, GlusterVolumeType.REPLICATE, 4)).when(volumeDao).getById(volumeId4);
        doReturn(getVdsStatic()).when(vdsStaticDao).get(serverId);
        doReturn(getCluster()).when(cmd).getCluster();
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(clusterId);
        cluster.setVirtService(false);
        cluster.setGlusterService(true);
        cluster.setCompatibilityVersion(Version.v4_2);
        return cluster;
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

    private GlusterVolumeEntity getVolume(Guid volumeId, GlusterVolumeType volumeType, int brickCount) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(volumeId);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        if (volumeType.equals(GlusterVolumeType.DISTRIBUTED_REPLICATE)) {
            volumeEntity.setStatus((volumeId == volumeId1) ? GlusterStatus.UP : GlusterStatus.DOWN);
            volumeEntity.setBricks(getBricks(volumeId, "distrib", 2));
            volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
            volumeEntity.setClusterId(clusterId);
            return volumeEntity;
        } else if (volumeType.equals(GlusterVolumeType.REPLICATE)) {
            volumeEntity.setStatus(GlusterStatus.UP);
            volumeEntity.setBricks(getBricks(volumeId, "repl", brickCount));
            volumeEntity.setVolumeType(GlusterVolumeType.REPLICATE);
            volumeEntity.setReplicaCount(brickCount);
            volumeEntity.setClusterId(clusterId);
            return volumeEntity;
        } else {
            return volumeEntity;
        }
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, String dirPrefix, int n) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick;
        for (Integer i = 0; i < n; i++) {
            brick = new GlusterBrickEntity();
            brick.setVolumeId(volumeId);
            brick.setServerId(serverId);
            brick.setServerName(serverName);
            brick.setBrickDirectory("/tmp/" + dirPrefix + i.toString());
            brick.setStatus(GlusterStatus.UP);
            bricks.add(brick);
        }
        return bricks;
    }

    private void setVolumeId(Guid volumeId) {
        cmd.setGlusterVolumeId(volumeId);
        cmd.getParameters().setExistingBrick(getBricks(volumeId, "distrib", 1).get(0));
    }

    @Test
    public void validateSucceedsOnUpVolume() {
        setVolumeId(volumeId1);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailesOnDownVolume() {
        setVolumeId(volumeId2);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnInvalidBrick() {
        setVolumeId(volumeId3);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        setVolumeId(null);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNoBrick() {
        cmd.setGlusterVolumeId(volumeId4);
        assertFalse(cmd.validate());
    }

}
