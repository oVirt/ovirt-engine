package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class PauseGlusterVolumeGeoRepSessionCommandTest extends GeoRepSessionCommandTest<PauseGlusterVolumeGeoRepSessionCommand> {

    @Override
    protected PauseGlusterVolumeGeoRepSessionCommand createCommand() {
        return new PauseGlusterVolumeGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(), null);
    }

    protected GlusterGeoRepSession getGeoRepSession(Guid gSessionId, GeoRepSessionStatus status, Guid masterVolumeID) {
        GlusterGeoRepSession session = super.getGeoRepSession(gSessionId, status);
        session.setMasterVolumeId(startedVolumeId);
        return session;
    }

    @Test
    public void validateSucceeds() {
        cmd.getParameters().setVolumeId(startedVolumeId);
        cmd.setGlusterVolumeId(startedVolumeId);
        cmd.getParameters().setGeoRepSessionId(geoRepSessionId);
        doReturn(getGeoRepSession(geoRepSessionId, GeoRepSessionStatus.ACTIVE, startedVolumeId)).when(geoRepDao)
.getById(geoRepSessionId);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFails() {
        cmd.getParameters().setVolumeId(startedVolumeId);
        cmd.setGlusterVolumeId(startedVolumeId);
        cmd.getParameters().setGeoRepSessionId(geoRepSessionId);
        doReturn(getGeoRepSession(geoRepSessionId, GeoRepSessionStatus.PASSIVE, startedVolumeId)).when(geoRepDao)
                .getById(geoRepSessionId);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        assertFalse(cmd.validate());
    }
}
