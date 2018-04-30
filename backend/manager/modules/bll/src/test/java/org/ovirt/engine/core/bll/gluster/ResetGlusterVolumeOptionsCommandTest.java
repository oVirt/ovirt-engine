package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ResetGlusterVolumeOptionsCommandTest extends BaseCommandTest {
    /**
     * The command under test.
     */
    private ResetGlusterVolumeOptionsCommand cmd;

    private final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    private ResetGlusterVolumeOptionsCommand createTestCommand(Guid volumeId) {
        return new ResetGlusterVolumeOptionsCommand(
                new ResetGlusterVolumeOptionsParameters(volumeId, null, false), null);
    }

    private void prepareMocks(ResetGlusterVolumeOptionsCommand command) {
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
    public void validateFailsOnNull() {
        cmd = spy(createTestCommand(null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

}
