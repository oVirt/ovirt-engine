package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;


public class RefreshGlusterVolumeDetailsCommandTest extends BaseCommandTest {

    private Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private Guid volumeId2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @Mock
    private GlusterVolumeDao volumeDao;

    /**
     * The command under test.
     */
    private RefreshGlusterVolumeDetailsCommand cmd;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private GlusterSyncJob syncJob;

    @Before
    public void init() {
        injectorRule.bind(BackendInternal.class, mock(BackendInternal.class));
        injectorRule.bind(AuditLogDirector.class, mock(AuditLogDirector.class));
        syncJob = Mockito.spy(GlusterSyncJob.getInstance());
    }

    private void prepareMocks(RefreshGlusterVolumeDetailsCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getDistributedVolume(volumeId1)).when(volumeDao).getById(volumeId1);
        doReturn(getDistributedVolume(volumeId2)).when(volumeDao).getById(volumeId2);
        doReturn(null).when(volumeDao).getById(null);
        doReturn(syncJob).when(command).getSyncJobInstance();
        doNothing().when(syncJob).refreshVolumeDetails(any(VDS.class), any(GlusterVolumeEntity.class));

    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private GlusterVolumeEntity getDistributedVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        volume.setStatus((volumeId == volumeId1) ? GlusterStatus.UP : GlusterStatus.DOWN);
        volume.setBricks(getBricks(volumeId, 2));
        volume.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volume.setClusterId(CLUSTER_ID);
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

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int n) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick;
        for (Integer i = 0; i < n; i++) {
            brick = new GlusterBrickEntity();
            brick.setVolumeId(volumeId);
            brick.setBrickDirectory("/tmp/test-vol" + i.toString());
            brick.setStatus(GlusterStatus.UP);
            bricks.add(brick);
        }
        return bricks;
    }

    protected VDS getServer() {
        VDS server =  new VDS();
        server.setId(Guid.newGuid());
        server.setVdsName("VDS1");
        server.setStatus(VDSStatus.Up);
        server.setClusterId(CLUSTER_ID);
        return server;
    }

    private RefreshGlusterVolumeDetailsCommand createTestCommand(Guid volumeId) {
        return new RefreshGlusterVolumeDetailsCommand(new GlusterVolumeParameters(volumeId), null);
    }

    @Test
    public void validateFailesOnDownVolume() {
        cmd = spy(createTestCommand(volumeId2));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd = spy(createTestCommand(null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateSucceedsOnUpVolume() {
        cmd = spy(createTestCommand(volumeId1));
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void executeCommand() {
        cmd = spy(createTestCommand(volumeId1));
        prepareMocks(cmd);
        cmd.executeCommand();
        assertEquals(cmd.getAuditLogTypeValue(), AuditLogType.GLUSTER_VOLUME_DETAILS_REFRESH);
        verify(syncJob, times(1)).refreshVolumeDetails(any(VDS.class), any(GlusterVolumeEntity.class));
    }
}
