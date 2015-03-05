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
import org.ovirt.engine.core.common.action.gluster.UpdateGlusterHostPubKeyToSlaveParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class UpdateGlusterHostPubKeyToSlaveInternalCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    private UpdateGlusterHostPubKeyToSlaveInternalCommand command;

    @Mock
    protected VDS vds;

    @Test
    public void commandSucceeds() {
        List<String> pubKeys = new ArrayList<>();
        pubKeys.add("");
        command =
                spy(new UpdateGlusterHostPubKeyToSlaveInternalCommand(new UpdateGlusterHostPubKeyToSlaveParameters(Guid.newGuid(),
                        pubKeys)));
        doReturn(vds).when(command).getUpServer();
        assertTrue(command.canDoAction());
    }

    @Test
    public void commandFailsNoPubKeys() {
        List<String> pubKeys = new ArrayList<>();
        command =
                spy(new UpdateGlusterHostPubKeyToSlaveInternalCommand(new UpdateGlusterHostPubKeyToSlaveParameters(Guid.newGuid(),
                        pubKeys)));
        doReturn(vds).when(command).getUpServer();
        assertFalse(command.canDoAction());
    }

    @Test
    public void commandFailsNoUpServer() {
        List<String> pubKeys = new ArrayList<>();
        pubKeys.add("");
        command =
                spy(new UpdateGlusterHostPubKeyToSlaveInternalCommand(new UpdateGlusterHostPubKeyToSlaveParameters(Guid.newGuid(),
                        pubKeys)));
        doReturn(null).when(command).getUpServer();
        command.setVdsGroupId(Guid.newGuid());
        doReturn(new VDSGroup()).when(command).getVdsGroup();
        assertFalse(command.canDoAction());
    }
}
