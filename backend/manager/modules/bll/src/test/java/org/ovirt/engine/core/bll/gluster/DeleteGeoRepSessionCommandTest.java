package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;

@MockitoSettings(strictness = Strictness.LENIENT)
public class DeleteGeoRepSessionCommandTest extends GeoRepSessionCommandTest<DeleteGeoRepSessionCommand> {

    @Override
    protected DeleteGeoRepSessionCommand createCommand() {
        return new DeleteGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(), null);
    }

    @Test
    public void validateSucceeds() {
        cmd.setGlusterVolumeId(startedVolumeId);
        cmd.getParameters().setGeoRepSessionId(geoRepSessionId);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFails() {
        cmd.setGlusterVolumeId(stoppedVolumeId);
        cmd.getParameters().setGeoRepSessionId(geoRepSessionId);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        assertFalse(cmd.validate());
    }

}
