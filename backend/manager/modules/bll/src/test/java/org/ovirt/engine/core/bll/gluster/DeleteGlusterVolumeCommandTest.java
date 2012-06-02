package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
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
@PrepareForTest({ DbFacade.class, GlusterVolumeDao.class, DeleteGlusterVolumeCommand.class, ClusterUtils.class })
public class DeleteGlusterVolumeCommandTest {
    @Mock
    DbFacade db;

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    ClusterUtils clusterUtils;

    private Guid stoppedVolumeId = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private Guid startedVolumeId = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    private DeleteGlusterVolumeCommand cmd;

    private DeleteGlusterVolumeCommand createTestCommand(Guid volumeId) {
        return new DeleteGlusterVolumeCommand(new GlusterVolumeParameters(volumeId));
    }

    @Before
    public void mockDbFacadeAndDao() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(GlusterVolumeDao.class);
        mockStatic(ClusterUtils.class);
        when(db.getGlusterVolumeDao()).thenReturn(volumeDao);
        when(DbFacade.getInstance()).thenReturn(db);
        when(volumeDao.getById(stoppedVolumeId)).thenReturn(getStoppedVolume(stoppedVolumeId));
        when(volumeDao.getById(startedVolumeId)).thenReturn(getStartedVolume(startedVolumeId));
        when(volumeDao.getById(null)).thenReturn(null);
        when(ClusterUtils.getInstance()).thenReturn(clusterUtils);
        when(clusterUtils.getUpServer(CLUSTER_ID)).thenReturn(getVds(VDSStatus.Up));
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.NewGuid());
        vds.setvds_name("gfs1");
        vds.setvds_group_id(CLUSTER_ID);
        vds.setstatus(status);
        return vds;
    }

    private GlusterVolumeEntity getStartedVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterVolumeStatus.UP);
        return volume;
    }

    private GlusterVolumeEntity getStoppedVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterVolumeStatus.DOWN);
        return volume;
    }

    private GlusterVolumeEntity getGlusterVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        HashSet<TransportType> transportType = new HashSet<TransportType>();
        HashSet<AccessProtocol> accessProtocol = new HashSet<AccessProtocol>();
        accessProtocol.add(AccessProtocol.GLUSTER);
        transportType.add(TransportType.TCP);

        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.setAccessProtocols(accessProtocol);
        volumeEntity.setTransportTypes(transportType);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        return volumeEntity;
    }

    @Test
    public void canDoActionSucceeds() {
        cmd = createTestCommand(stoppedVolumeId);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFails() {
        cmd = createTestCommand(startedVolumeId);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd = createTestCommand(null);
        assertFalse(cmd.canDoAction());
    }

}
