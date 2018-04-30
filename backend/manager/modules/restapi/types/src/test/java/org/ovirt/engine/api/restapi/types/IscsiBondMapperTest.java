package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageConnections;

public class IscsiBondMapperTest extends AbstractInvertibleMappingTest<IscsiBond,
        org.ovirt.engine.core.common.businessentities.IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond> {

    public IscsiBondMapperTest() {
        super(IscsiBond.class,
                org.ovirt.engine.core.common.businessentities.IscsiBond.class,
                org.ovirt.engine.core.common.businessentities.IscsiBond.class);
    }

    @Override
    protected void verify(IscsiBond model, IscsiBond transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());

        if (model.isSetDataCenter()) {
            assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
        } else {
            assertNull(transform.getDataCenter());
        }

        verifyNetworks(model.getNetworks(), transform.getNetworks());
        verifyStorageConnections(model.getStorageConnections(), transform.getStorageConnections());
    }

    private static void verifyNetworks(Networks before, Networks after) {
        if (before == null) {
            assertNull(after);
        } else {
            assertEquals(before.getNetworks().size(), after.getNetworks().size());

            Set<String> ids = new HashSet<>();

            for (Network network : before.getNetworks()) {
                ids.add(network.getId());
            }

            for (Network network : after.getNetworks()) {
                ids.remove(network.getId());
            }

            assertEquals(0, ids.size());
        }
    }

    private static void verifyStorageConnections(StorageConnections before, StorageConnections after) {
        if (before == null) {
            assertNull(after);
        } else {
            assertEquals(before.getStorageConnections().size(), after.getStorageConnections().size());

            Set<String> ids = new HashSet<>();

            for (StorageConnection conn : before.getStorageConnections()) {
                ids.add(conn.getId());
            }

            for (StorageConnection conn : after.getStorageConnections()) {
                ids.remove(conn.getId());
            }

            assertEquals(0, ids.size());
        }
    }
}
