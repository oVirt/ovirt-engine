package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;

@RunWith(MockitoJUnitRunner.class)
public class StopGeoRepSessionCommandTest extends GeoRepSessionCommandTest {

    /**
     * The command under test.
     */
    private StopGeoRepSessionCommand cmd;

    @Test
    public void canDoActionSucceeds() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(startedVolumeId, geoRepSessionId)));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFails() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(stoppedVolumeId, geoRepSessionId)));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsIfStopped() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(stoppedVolumeId, geoRepSessionId)));
        prepareMocks(cmd);
        doReturn(getGeoRepSession(geoRepSessionId, GeoRepSessionStatus.STOPPED)).when(geoRepDao).getById(geoRepSessionId);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(null, null)));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

}
