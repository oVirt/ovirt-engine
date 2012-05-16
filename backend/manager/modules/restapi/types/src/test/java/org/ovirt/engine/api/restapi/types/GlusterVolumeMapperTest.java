package org.ovirt.engine.api.restapi.types;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.api.model.AccessProtocol;
import org.ovirt.engine.api.model.GlusterState;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumeType;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.TransportType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;

public class GlusterVolumeMapperTest extends AbstractInvertibleMappingTest<GlusterVolume, GlusterVolumeEntity, GlusterVolumeEntity> {

    public GlusterVolumeMapperTest() {
        super(GlusterVolume.class, GlusterVolumeEntity.class, GlusterVolumeEntity.class);
    }

    @Override
    protected GlusterVolume postPopulate(GlusterVolume model) {
        // The model is pre-populated with randomly generated values.
        // This won't work for enum fields like volume type since it
        // must be a valid value from corresponding enum
        model.setVolumeType(GlusterVolumeType.DISTRIBUTE.name());

        List<String> transportTypes = model.getTransportTypes().getTransportTypes();
        transportTypes.clear();
        transportTypes.add(TransportType.TCP.name());

        List<String> accessProtocols = model.getAccessProtocols().getAccessProtocols();
        accessProtocols.clear();
        accessProtocols.add(AccessProtocol.GLUSTER.name());
        accessProtocols.add(AccessProtocol.NFS.name());

        model.getAccessControlList().getAccessControlList().add("*");

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

        verifyOptions(model, transform);
    }

    /**
     * this test was added to support 'status' field, which has only a one-way mapping (from Backend entity to REST
     * entity). The generic test does a round-trip, which would fail on status comparison when there's only one-way mapping.
     */
    @Test
    public void testFromBackendToRest() {
        testStatusMapping(GlusterVolumeStatus.UP, GlusterState.UP);
        testStatusMapping(GlusterVolumeStatus.DOWN, GlusterState.DOWN);
    }

    private void testStatusMapping(GlusterVolumeStatus backendStatus, GlusterState restStatus) {
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setStatus(backendStatus);
        GlusterVolume restVolume = GlusterVolumeMapper.map(volume, null);
        assertEquals(restVolume.getState(), restStatus.value());
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
