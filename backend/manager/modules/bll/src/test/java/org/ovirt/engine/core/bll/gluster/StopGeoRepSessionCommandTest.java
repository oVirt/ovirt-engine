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
    public void validateSucceeds() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(startedVolumeId, geoRepSessionId), null));
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFails() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(stoppedVolumeId, geoRepSessionId), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsIfStopped() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(stoppedVolumeId, geoRepSessionId), null));
        prepareMocks(cmd);
        doReturn(getGeoRepSession(geoRepSessionId, GeoRepSessionStatus.STOPPED)).when(geoRepDao).getById(geoRepSessionId);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd = spy(new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(null, null), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }

}
