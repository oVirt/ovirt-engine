package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;

@MockitoSettings(strictness = Strictness.LENIENT)
public class StopGeoRepSessionCommandTest extends GeoRepSessionCommandTest<StopGeoRepSessionCommand> {

    @Override
    protected StopGeoRepSessionCommand createCommand() {
        return new StopGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(), null);
    }

    @Test
    public void validateSucceeds() {
        cmd.getParameters().setVolumeId(startedVolumeId);
        cmd.setGlusterVolumeId(startedVolumeId);
        cmd.getParameters().setGeoRepSessionId(geoRepSessionId);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFails() {
        cmd.getParameters().setVolumeId(stoppedVolumeId);
        cmd.setGlusterVolumeId(stoppedVolumeId);
        cmd.getParameters().setGeoRepSessionId(geoRepSessionId);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsIfStopped() {
        cmd.getParameters().setVolumeId(stoppedVolumeId);
        cmd.setGlusterVolumeId(stoppedVolumeId);
        cmd.getParameters().setGeoRepSessionId(geoRepSessionId);
        doReturn(getGeoRepSession(geoRepSessionId, GeoRepSessionStatus.STOPPED)).when(geoRepDao).getById(geoRepSessionId);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        assertFalse(cmd.validate());
    }
}
