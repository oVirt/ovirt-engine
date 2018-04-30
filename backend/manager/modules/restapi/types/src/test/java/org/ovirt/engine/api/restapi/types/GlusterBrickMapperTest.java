package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterBrickStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;

public class GlusterBrickMapperTest extends AbstractInvertibleMappingTest<GlusterBrick, GlusterBrickEntity, GlusterBrickEntity> {

    public GlusterBrickMapperTest() {
        super(GlusterBrick.class, GlusterBrickEntity.class, GlusterBrickEntity.class);
    }

    @Override
    protected void verify(GlusterBrick model, GlusterBrick transform) {
        assertNotNull(transform);

        assertNotNull(transform.getServerId());
        assertEquals(model.getServerId(), transform.getServerId());

        assertNotNull(transform.getBrickDir());
        assertEquals(model.getBrickDir(), transform.getBrickDir());
    }

    /**
     * this test was added to support 'status' field, which has only a one-way mapping (from Backend entity to REST
     * entity). The generic test does a round-trip, which would fail on status comparison when there's only one-way mapping.
     */
    @Test
    public void testFromBackendToRest() {
        testStatusMapping(GlusterStatus.UP, GlusterBrickStatus.UP);
        testStatusMapping(GlusterStatus.DOWN, GlusterBrickStatus.DOWN);
    }

    private void testStatusMapping(GlusterStatus backendStatus, GlusterBrickStatus restStatus) {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setStatus(backendStatus);
        GlusterBrick restBrick = GlusterBrickMapper.map(brick, null);
        assertEquals(restBrick.getStatus(), restStatus);
    }
}
