package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;

@RunWith(MockitoJUnitRunner.class)
public class DeleteGeoRepSessionCommandTest extends GeoRepSessionCommandTest {

    /**
     * The command under test.
     */
    private DeleteGeoRepSessionCommand cmd;

    @Test
    public void canDoActionSucceeds() {
        cmd = spy(new DeleteGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(startedVolumeId, geoRepSessionId)));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFails() {
        cmd = spy(new DeleteGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(stoppedVolumeId, geoRepSessionId)));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd = spy(new DeleteGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(null, null)));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

}
