package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class StartGlusterVolumeCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    private Guid startedVolumeId = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private Guid stoppedVolumeId = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    /**
     * The command under test.
     */
    private StartGlusterVolumeCommand cmd;

    private void prepareMocks(StartGlusterVolumeCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getGlusterVolume(stoppedVolumeId)).when(volumeDao).getById(stoppedVolumeId);
        doReturn(getGlusterVolume(startedVolumeId)).when(volumeDao).getById(startedVolumeId);
        doReturn(null).when(volumeDao).getById(null);
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private GlusterVolumeEntity getGlusterVolume(Guid volumeId) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(volumeId);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volumeEntity.setStatus(volumeId.equals(startedVolumeId) ? GlusterStatus.UP : GlusterStatus.DOWN);
        volumeEntity.setClusterId(CLUSTER_ID);
        return volumeEntity;
    }

    @Test
    public void validateSucceeds() {
        cmd = spy(new StartGlusterVolumeCommand(new GlusterVolumeActionParameters(stoppedVolumeId, false), null));
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFails() {
        cmd = spy(new StartGlusterVolumeCommand(new GlusterVolumeActionParameters(startedVolumeId, false), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd = spy(new StartGlusterVolumeCommand(new GlusterVolumeActionParameters(null, false), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

}
