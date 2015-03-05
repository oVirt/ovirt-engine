package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.SetUpMountBrokerParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class SetupGlusterGeoRepMountBrokerInternalCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    VdsDAO vdsDao;

    private SetupGlusterGeoRepMountBrokerInternalCommand command;

    @Mock
    GlusterVolumeEntity volume;

    @Mock
    VDS vds;

    @Test
    public void commandSucceeds() {
        command =
                spy(new SetupGlusterGeoRepMountBrokerInternalCommand(new SetUpMountBrokerParameters(Guid.newGuid(),
                        "",
                        null,
                        null)));
        doReturn(vds).when(command).getUpServer();
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(GlusterStatus.UP).when(volume).getStatus();
        assertTrue(command.canDoAction());
    }

    @Test
    public void commandFailsSlaveNotOvirtMonitored() {
        command =
                spy(new SetupGlusterGeoRepMountBrokerInternalCommand(new SetUpMountBrokerParameters(Guid.newGuid(),
                        "",
                        null,
                        null)));
        doReturn(vds).when(command).getUpServer();
        doReturn(null).when(command).getSlaveVolume();
        assertFalse(command.canDoAction());
    }

    @Test
    public void commandFailsSlaveVolumeNotUp() {
        command =
                spy(new SetupGlusterGeoRepMountBrokerInternalCommand(new SetUpMountBrokerParameters(Guid.newGuid(),
                        "",
                        null,
                        null)));
        doReturn(vds).when(command).getUpServer();
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(GlusterStatus.DOWN).when(volume).getStatus();
        assertFalse(command.canDoAction());
    }
}
