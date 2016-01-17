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
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class PauseGlusterVolumeGeoRepSessionCommandTest extends GeoRepSessionCommandTest {

    private PauseGlusterVolumeGeoRepSessionCommand cmd;

    protected GlusterGeoRepSession getGeoRepSession(Guid gSessionId, GeoRepSessionStatus status, Guid masterVolumeID) {
        GlusterGeoRepSession session = super.getGeoRepSession(gSessionId, status);
        session.setMasterVolumeId(startedVolumeId);
        return session;
    }

    @Test
    public void validateSucceeds() {
        GlusterVolumeGeoRepSessionParameters param = new GlusterVolumeGeoRepSessionParameters();
        param.setForce(false);
        param.setGeoRepSessionId(geoRepSessionId);
        param.setVolumeId(startedVolumeId);
        cmd = spy(new PauseGlusterVolumeGeoRepSessionCommand(param, null));
        doReturn(getGeoRepSession(geoRepSessionId, GeoRepSessionStatus.ACTIVE, startedVolumeId)).when(geoRepDao)
                .getById(geoRepSessionId);
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFails() {
        GlusterVolumeGeoRepSessionParameters param = new GlusterVolumeGeoRepSessionParameters();
        param.setForce(false);
        param.setVolumeId(startedVolumeId);
        param.setGeoRepSessionId(geoRepSessionId);
        cmd = spy(new PauseGlusterVolumeGeoRepSessionCommand(param, null));
        prepareMocks(cmd);
        doReturn(getGeoRepSession(geoRepSessionId, GeoRepSessionStatus.PASSIVE, startedVolumeId)).when(geoRepDao)
                .getById(geoRepSessionId);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        cmd = spy(new PauseGlusterVolumeGeoRepSessionCommand(new GlusterVolumeGeoRepSessionParameters(), null));
        prepareMocks(cmd);
        assertFalse(cmd.validate());
    }
}
