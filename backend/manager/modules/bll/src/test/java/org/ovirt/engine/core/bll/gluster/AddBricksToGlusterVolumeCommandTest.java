package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class AddBricksToGlusterVolumeCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    VdsStaticDAO vdsStaticDao;

    @Mock
    GlusterBrickDao brickDao;

    @Mock
    VdsGroupDAO vdsGroupDao;

    private String serverName = "myhost";

    private Guid clusterId = new Guid("c0dd8ca3-95dd-44ad-a88a-440a6e3d8106");

    private Guid serverId = new Guid("d7f10a21-bbf2-4ffd-aab6-4da0b3b2ccec");

    private Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");

    private Guid volumeId2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    /**
     * The command under test.
     */
    private AddBricksToGlusterVolumeCommand cmd;

    private AddBricksToGlusterVolumeCommand createTestCommand(Guid volumeId,
            List<GlusterBrickEntity> bricks,
            int replicaCount,
            int stripeCount) {
        return new AddBricksToGlusterVolumeCommand(new GlusterVolumeBricksActionParameters(volumeId,
                bricks,
                replicaCount,
                stripeCount));
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int max) {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        GlusterBrickEntity brick;
        for (Integer i = 0; i < max; i++) {
            brick =
                    new GlusterBrickEntity(volumeId,
                            new VdsStatic(serverName,
                                    "127.0.0.1",
                                    "0934390834",
                                    20,
                                    new Guid(),
                                    serverId,
                                    serverName,
                                    true,
                                    VDSType.oVirtNode),
                            "/tmp/s" + i.toString(),
                            GlusterStatus.UP);
            bricks.add(brick);
        }
        return bricks;
    }

    private List<GlusterBrickEntity> getBricks(Guid glusterServerId) {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        GlusterBrickEntity brick;
        for (Integer i = 0; i < 5; i++) {
            brick =
                    new GlusterBrickEntity(volumeId1,
                            new VdsStatic(serverName,
                                    "127.0.0.1",
                                    "0934390834",
                                    20,
                                    new Guid(),
                                    glusterServerId,
                                    serverName,
                                    true,
                                    VDSType.oVirtNode),
                            "/tmp/s" + i.toString(),
                            GlusterStatus.UP);
            bricks.add(brick);
        }
        return bricks;
    }
    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.NewGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(clusterId);
        vds.setStatus(status);
        return vds;
    }

    private void prepareMocks(AddBricksToGlusterVolumeCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(vdsStaticDao).when(command).getVdsStaticDao();
        doReturn(brickDao).when(command).getGlusterBrickDao();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getSingleBrickVolume(volumeId1)).when(volumeDao).getById(volumeId1);
        doReturn(getMultiBrickVolume(volumeId2)).when(volumeDao).getById(volumeId2);
        doReturn(getBricks(serverId)).when(brickDao).getGlusterVolumeBricksByServerId(serverId);
        doReturn(null).when(volumeDao).getById(null);
        doReturn(getVdsStatic()).when(vdsStaticDao).get(serverId);
        doReturn(getVDsGroup()).when(command).getVdsGroup();
    }

    private VDSGroup getVDsGroup() {
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setId(clusterId);
        vdsGroup.setVirtService(false);
        vdsGroup.setGlusterService(true);
        vdsGroup.setcompatibility_version(Version.v3_1);
        return vdsGroup;
    }

    private VdsStatic getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setVdsGroupId(clusterId);
        vds.setHostName(serverName);
        return vds;
    }

    private GlusterVolumeEntity getSingleBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, 1));
        volume.setClusterId(clusterId);
        return volume;
    }

    private GlusterVolumeEntity getMultiBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, 2));
        volume.setClusterId(clusterId);
        return volume;
    }

    private GlusterVolumeEntity getGlusterVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volumeEntity.setClusterId(clusterId);
        return volumeEntity;
    }

    @Test
    public void canDoActionSucceeds() {
        cmd = spy(createTestCommand(volumeId2, getBricks(volumeId2, 1), 2, 0));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFails() {
        cmd = spy(createTestCommand(volumeId1, getBricks(volumeId1, 2), 0, 4));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd = spy(createTestCommand(null, null, 0, 0));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

}
