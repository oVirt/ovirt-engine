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
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
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
        for (Integer i = 0; i < max; i++) {
            GlusterBrickEntity brick1 = new GlusterBrickEntity();
            brick1.setVolumeId(volumeId);
            brick1.setServerName("server1");
            brick1.setStatus(GlusterBrickStatus.UP);
            brick1.setBrickDirectory("/tmp/s" + i.toString());
            bricks.add(brick1);
        }
        return bricks;
    }

    @Before
    public void mockDbFacadeAndDao() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(GlusterVolumeDao.class);
        when(db.getGlusterVolumeDao()).thenReturn(volumeDao);
        when(DbFacade.getInstance()).thenReturn(db);
        when(volumeDao.getById(volumeId1)).thenReturn(getSingleBrickVolume(volumeId1));
        when(volumeDao.getById(volumeId2)).thenReturn(getMultiBrickVolume(volumeId2));
        when(volumeDao.getById(null)).thenReturn(null);
    }

    private GlusterVolumeEntity getSingleBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterVolumeStatus.UP);
        volume.setBricks(getBricks(volumeId, 1));
        return volume;
    }

    private GlusterVolumeEntity getMultiBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterVolumeStatus.UP);
        volume.setBricks(getBricks(volumeId, 2));
        return volume;
    }

    private GlusterVolumeEntity getGlusterVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
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
