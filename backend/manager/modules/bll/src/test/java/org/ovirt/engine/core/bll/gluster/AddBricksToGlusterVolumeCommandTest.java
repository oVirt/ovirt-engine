package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, GlusterVolumeDao.class, AddBricksToGlusterVolumeCommand.class })
public class AddBricksToGlusterVolumeCommandTest {
    @Mock
    DbFacade db;

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    VdsStaticDAO vdsStaticDao;

    private String serverName = "myhost";
    private Guid clusterId = new Guid("c0dd8ca3-95dd-44ad-a88a-440a6e3d8106");
    private Guid serverId = new Guid("d7f10a21-bbf2-4ffd-aab6-4da0b3b2ccec");
    private Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");

    private Guid volumeId2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

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
                            GlusterBrickStatus.UP);
            bricks.add(brick);
        }
        return bricks;
    }

    @Before
    public void mockDbFacadeAndDao() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(GlusterVolumeDao.class);
        when(db.getGlusterVolumeDao()).thenReturn(volumeDao);
        when(db.getVdsStaticDAO()).thenReturn(vdsStaticDao);
        when(DbFacade.getInstance()).thenReturn(db);
        when(volumeDao.getById(volumeId1)).thenReturn(getSingleBrickVolume(volumeId1));
        when(volumeDao.getById(volumeId2)).thenReturn(getMultiBrickVolume(volumeId2));
        when(volumeDao.getById(null)).thenReturn(null);
        when(vdsStaticDao.get(serverId)).thenReturn(getVdsStatic());
    }

    private VdsStatic getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setvds_group_id(clusterId);
        vds.sethost_name(serverName);
        return vds;
    }

    private GlusterVolumeEntity getSingleBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterVolumeStatus.UP);
        volume.setBricks(getBricks(volumeId, 1));
        volume.setClusterId(clusterId);
        return volume;
    }

    private GlusterVolumeEntity getMultiBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterVolumeStatus.UP);
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
        cmd = createTestCommand(volumeId2, getBricks(volumeId2, 1), 2, 0);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFails() {
        cmd = createTestCommand(volumeId1, getBricks(volumeId1, 2), 0, 4);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd = createTestCommand(null, null, 0, 0);
        assertFalse(cmd.canDoAction());
    }

}
