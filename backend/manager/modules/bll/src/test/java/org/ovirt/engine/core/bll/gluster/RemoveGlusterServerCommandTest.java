package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.gluster.RemoveGlusterServerParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

public class RemoveGlusterServerCommandTest extends BaseCommandTest {

    private static final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final String SERVER_NAME = "server1";

    // The command under test.
    private RemoveGlusterServerCommand cmd;

    private void prepareMocks(RemoveGlusterServerCommand command) {
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    @Test
    public void validateSucceeds() {
        cmd = spy(new RemoveGlusterServerCommand(
                new RemoveGlusterServerParameters(CLUSTER_ID, SERVER_NAME, false), null));
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd = spy(new RemoveGlusterServerCommand(new RemoveGlusterServerParameters(CLUSTER_ID, null, false), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }
}
