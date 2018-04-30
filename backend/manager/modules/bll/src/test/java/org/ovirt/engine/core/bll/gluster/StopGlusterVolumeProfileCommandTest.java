package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class StopGlusterVolumeProfileCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    private static final Guid STARTED_VOLUME_ID = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private static final Guid STOPPED_VOLUME_ID = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @Spy
    @InjectMocks
    private StopGlusterVolumeProfileCommand cmd =
            new StopGlusterVolumeProfileCommand(new GlusterVolumeParameters(), null);

    @BeforeEach
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getGlusterVolume(STOPPED_VOLUME_ID)).when(volumeDao).getById(STOPPED_VOLUME_ID);
        doReturn(getGlusterVolume(STARTED_VOLUME_ID)).when(volumeDao).getById(STARTED_VOLUME_ID);
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("server1");
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
        volumeEntity.setStatus(volumeId.equals(STARTED_VOLUME_ID) ? GlusterStatus.UP : GlusterStatus.DOWN);
        volumeEntity.setClusterId(CLUSTER_ID);
        return volumeEntity;
    }

    @Test
    public void validateSucceedsOnStoppedVolume() {
        cmd.setGlusterVolumeId(STOPPED_VOLUME_ID);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateSucceedsOnStartedVolume() {
        cmd.setGlusterVolumeId(STARTED_VOLUME_ID);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        assertFalse(cmd.validate());
    }
}
