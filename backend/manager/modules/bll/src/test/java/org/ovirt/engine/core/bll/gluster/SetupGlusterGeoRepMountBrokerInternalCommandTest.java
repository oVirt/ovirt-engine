package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.gluster.SetUpMountBrokerParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class SetupGlusterGeoRepMountBrokerInternalCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    VdsDao vdsDao;

    private SetupGlusterGeoRepMountBrokerInternalCommand command;

    @Mock
    GlusterVolumeEntity volume;

    @Mock
    VDS vds;

    @Test
    public void commandSucceeds() {
        command =
                spy(new SetupGlusterGeoRepMountBrokerInternalCommand(new SetUpMountBrokerParameters(Guid.newGuid(),
                        new HashSet<>(Collections.singletonList(Guid.newGuid())),
                        null,
                        null), null));
        doReturn(vds).when(command).getUpServer();
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(GlusterStatus.UP).when(volume).getStatus();
        assertTrue(command.validate());
    }

    @Test
    public void commandFailsSlaveNotOvirtMonitored() {
        command =
                spy(new SetupGlusterGeoRepMountBrokerInternalCommand(new SetUpMountBrokerParameters(Guid.newGuid(),
                        new HashSet<>(Collections.singletonList(Guid.newGuid())),
                        null,
                        null), null));
        doReturn(null).when(command).getSlaveVolume();
        assertFalse(command.validate());
    }

    @Test
    public void commandFailsSlaveVolumeNotUp() {
        command =
                spy(new SetupGlusterGeoRepMountBrokerInternalCommand(new SetUpMountBrokerParameters(Guid.newGuid(),
                        new HashSet<>(Collections.singletonList(Guid.newGuid())),
                        null,
                        null), null));
        doReturn(volume).when(command).getSlaveVolume();
        doReturn(GlusterStatus.DOWN).when(volume).getStatus();
        assertFalse(command.validate());
    }
}
