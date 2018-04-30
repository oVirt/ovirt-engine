package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumeStatus;
import org.ovirt.engine.api.model.GlusterVolumeType;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.TransportType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;

public class GlusterVolumeMapperTest extends AbstractInvertibleMappingTest<GlusterVolume, GlusterVolumeEntity, GlusterVolumeEntity> {

    public GlusterVolumeMapperTest() {
        super(GlusterVolume.class, GlusterVolumeEntity.class, GlusterVolumeEntity.class);
    }

    @Override
    protected GlusterVolume postPopulate(GlusterVolume model) {
        // The model is pre-populated with randomly generated values.
        // This won't work for enum fields like volume type since it
        // must be a valid value from corresponding enum
        model.setVolumeType(GlusterVolumeType.DISTRIBUTE);

        List<TransportType> transportTypes = model.getTransportTypes().getTransportTypes();
        transportTypes.clear();
        transportTypes.add(TransportType.TCP);

        return model;
    }

    @Override
    protected void verify(GlusterVolume model, GlusterVolume transform) {
        assertNotNull(transform);

        assertNotNull(transform.getId());
        assertEquals(model.getId(), transform.getId());

        assertNotNull(transform.getName());
        assertEquals(model.getName(), transform.getName());

        assertNotNull(transform.getVolumeType());
        assertEquals(model.getVolumeType(), transform.getVolumeType());

        assertNotNull(transform.getTransportTypes());
        assertArrayEquals(model.getTransportTypes().getTransportTypes().toArray(),
                transform.getTransportTypes().getTransportTypes().toArray());

        assertNotNull(transform.getReplicaCount());
        assertEquals(model.getReplicaCount(), transform.getReplicaCount());

        assertNotNull(transform.getStripeCount());
        assertEquals(model.getStripeCount(), transform.getStripeCount());

        assertNotNull(transform.getDisperseCount());
        assertEquals(model.getDisperseCount(), transform.getDisperseCount());

        assertNotNull(transform.getRedundancyCount());
        assertEquals(model.getRedundancyCount(), transform.getRedundancyCount());

        verifyOptions(model, transform);
    }

    /**
     * this test was added to support 'status' field, which has only a one-way mapping (from Backend entity to REST
     * entity). The generic test does a round-trip, which would fail on status comparison when there's only one-way mapping.
     */
    @Test
    public void testFromBackendToRest() {
        testStatusMapping(GlusterStatus.UP, GlusterVolumeStatus.UP);
        testStatusMapping(GlusterStatus.DOWN, GlusterVolumeStatus.DOWN);
    }

    private void testStatusMapping(GlusterStatus backendStatus, GlusterVolumeStatus restStatus) {
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setStatus(backendStatus);
        GlusterVolume restVolume = GlusterVolumeMapper.map(volume, null);
        assertEquals(restVolume.getStatus(), restStatus);
    }

    private boolean containsOption(GlusterVolume volume, Option expectedOption) {
        for (Option option : volume.getOptions().getOptions()) {
            if (option.getName().equals(expectedOption.getName())
                    && option.getValue().equals(expectedOption.getValue())) {
                return true;
            }
        }
        return false;
    }

    private void verifyOptions(GlusterVolume model, GlusterVolume transform) {
        for (Option modelOption : model.getOptions().getOptions()) {
            assertTrue(containsOption(transform, modelOption));
        }
    }
}
