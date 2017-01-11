package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;

@RunWith(MockitoJUnitRunner.class)
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
