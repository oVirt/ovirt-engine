package org.ovirt.engine.core.bll.gluster;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class CreateGlusterVolumeGeoRepSessionCommandTest {
    private static final Version NOT_SUPPORTED_VERSION = Version.v3_4;

    private static final Object SUPPORTED_VERSION = Version.v3_6;

    CreateGlusterVolumeGeoRepSessionCommand command;

    private final String slaveVolumeName = "slaveVol";
    private final String slaveHost = "localhost.localdomain";
    private final Guid masterVolumeId = Guid.newGuid();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterGeoReplicationEnabled, Version.v3_6.toString(), true),
            mockConfig(ConfigValues.GlusterGeoReplicationEnabled, NOT_SUPPORTED_VERSION.toString(), false)
            );

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    VdsDAO vdsDao;

    @Mock
    VdsGroupDAO vdsGroupDao;

    @Mock
    GlusterGeoRepDao geoRepDao;

    @Mock
    protected VDSGroup vdsGroup;

    @Mock
    protected GlusterVolumeEntity volume;

    @Mock
    protected VDS vds;

    @Test
    public void commandSucceeds() {
        command =
                spy(new CreateGlusterVolumeGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                        slaveVolumeName,
                        Guid.newGuid(),
                        null,
                        null,
                        false)));
        prepareMocks();
        doReturn(SUPPORTED_VERSION).when(vdsGroup).getCompatibilityVersion();
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(null).when(geoRepDao).getGeoRepSession(any(Guid.class), any(Guid.class), any(String.class));
        doReturn(vds).when(command).getSlaveHost();
        assertTrue(command.canDoAction());
    }

    private void prepareMocks() {
        doReturn(volume).when(volumeDao).getById(masterVolumeId);
        doReturn(GlusterStatus.UP).when(volume).getStatus();
        doReturn(vdsGroup).when(command).getVdsGroup();
        doReturn(vdsGroupDao).when(command).getVdsGroupDAO();
        doReturn(vdsDao).when(command).getVdsDAO();
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(geoRepDao).when(command).getGeoRepDao();
        doReturn(vds).when(command).getUpServer();
        doReturn(VDSStatus.Up).when(vds).getStatus();
    }

    @Test
    public void commandFailsSlaveVolumeNotMonitoredByOvirt() {
        command =
                spy(new CreateGlusterVolumeGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                        slaveVolumeName,
                        Guid.newGuid(),
                        null,
                        null,
                        false)));
        prepareMocks();
        doReturn(null).when(command).getSlaveVolume();
        doReturn(SUPPORTED_VERSION).when(vdsGroup).getCompatibilityVersion();
        doReturn(vds).when(command).getSlaveHost();
        doReturn(null).when(geoRepDao).getGeoRepSession(any(Guid.class), any(Guid.class), any(String.class));
        assertFalse(command.canDoAction());
    }

    @Test
    public void commandFailsSessionExists() {
        command =
                spy(new CreateGlusterVolumeGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                        slaveVolumeName,
                        Guid.newGuid(),
                        null,
                        null,
                        false)));
        prepareMocks();
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(vds).when(command).getSlaveHost();
        doReturn(SUPPORTED_VERSION).when(vdsGroup).getCompatibilityVersion();
        doReturn(new GlusterGeoRepSession()).when(geoRepDao).getGeoRepSession(any(Guid.class),
                any(Guid.class),
                any(String.class));
        assertFalse(command.canDoAction());
    }

    @Test
    public void commandFailsVersionNotSupported() {
        command =
                spy(new CreateGlusterVolumeGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(Guid.newGuid(),
                        slaveVolumeName,
                        Guid.newGuid(),
                        null,
                        null,
                        false)));
        prepareMocks();
        doReturn(vds).when(command).getUpServer();
        doReturn(vds).when(command).getSlaveHost();
        doReturn(NOT_SUPPORTED_VERSION).when(vdsGroup).getCompatibilityVersion();
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(null).when(geoRepDao).getGeoRepSession(any(Guid.class), any(Guid.class), any(String.class));
        assertFalse(command.canDoAction());
    }

    @Test
    public void commandFailsSlaveHostInvalid() {
        command =
                spy(new CreateGlusterVolumeGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                        slaveVolumeName,
                        Guid.newGuid(),
                        null,
                        null,
                        false)));
        prepareMocks();
        doReturn(vds).when(command).getUpServer();
        doReturn(SUPPORTED_VERSION).when(vdsGroup).getCompatibilityVersion();
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(null).when(geoRepDao).getGeoRepSession(any(Guid.class), any(Guid.class), any(String.class));
        doReturn(null).when(command).getSlaveHost();
        assertFalse(command.canDoAction());
    }
}
