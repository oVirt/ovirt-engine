package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
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
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class ReplaceGlusterVolumeBrickCommandTest extends BaseCommandTest {

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
    private ReplaceGlusterVolumeBrickCommand cmd;

    private void prepareMocks(ReplaceGlusterVolumeBrickCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(vdsStaticDao).when(command).getVdsStaticDao();
        doReturn(networkDao).when(command).getNetworkDao();
        doReturn(interfaceDao).when(command).getInterfaceDao();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getDistributedVolume(volumeId1)).when(volumeDao).getById(volumeId1);
        doReturn(getDistributedVolume(volumeId2)).when(volumeDao).getById(volumeId2);
        doReturn(getReplicatedVolume(volumeId3, 2)).when(volumeDao).getById(volumeId3);
        doReturn(getReplicatedVolume(volumeId4, 4)).when(volumeDao).getById(volumeId4);
        doReturn(null).when(volumeDao).getById(null);
        doReturn(getVdsStatic()).when(vdsStaticDao).get(serverId);
        doReturn(getCluster()).when(command).getCluster();
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(clusterId);
        cluster.setVirtService(false);
        cluster.setGlusterService(true);
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

    private GlusterVolumeEntity getDistributedVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        volume.setStatus((volumeId == volumeId1) ? GlusterStatus.UP : GlusterStatus.DOWN);
        volume.setBricks(getBricks(volumeId, "distrib", 2));
        volume.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
        volume.setClusterId(clusterId);
        return volume;
    }

    private GlusterVolumeEntity getReplicatedVolume(Guid volumeId, int brickCount) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, "repl", brickCount));
        volume.setVolumeType(GlusterVolumeType.REPLICATE);
        volume.setReplicaCount(brickCount);
        volume.setClusterId(clusterId);
        return volume;
    }

    private GlusterVolumeEntity getVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        return volumeEntity;
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

    private ReplaceGlusterVolumeBrickCommand createTestCommand1(Guid volumeId) {
        return new ReplaceGlusterVolumeBrickCommand(new GlusterVolumeReplaceBrickActionParameters(volumeId,
                getBricks(volumeId, "distrib", 1).get(0),
                getBricks(volumeId, "new", 1).get(0)), null);
    }

    private ReplaceGlusterVolumeBrickCommand createTestCommand2(Guid volumeId) {
        return new ReplaceGlusterVolumeBrickCommand(new GlusterVolumeReplaceBrickActionParameters(volumeId,
                null,
                getBricks(volumeId, "", 1).get(0)), null);
    }

    @Test
    public void validateSucceedsOnUpVolume() {
        cmd = spy(createTestCommand1(volumeId1));
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailesOnDownVolume() {
        cmd = spy(createTestCommand1(volumeId2));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnInvalidBrick() {
        cmd = spy(createTestCommand1(volumeId3));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd = spy(createTestCommand1(null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNoBrick() {
        cmd = spy(createTestCommand2(volumeId4));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

}
