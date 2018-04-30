package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class NetworkViewDaoTest extends BaseDaoTestCase<NetworkViewDao> {
    /**
     * Test query
     */
    @Test
    public void testGetAllWithQuery() {
        List<NetworkView> result =
                dao.getAllWithQuery(String.format("SELECT * FROM network_view where id = '%s'",
                        FixturesTool.NETWORK_ENGINE));

        assertFalse(result.isEmpty());
        assertEquals(FixturesTool.NETWORK_ENGINE, result.get(0).getId());
    }

    /**
     * Ensures the right set of networks are returned for the given provider.
     */
    @Test
    public void testGetAllForProvider() {
        List<NetworkView> result = dao.getAllForProvider(FixturesTool.PROVIDER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (NetworkView network : result) {
            assertEquals(FixturesTool.PROVIDER_ID, network.getProvidedBy().getProviderId());
            assertEquals(FixturesTool.PROVIDER_NAME, network.getProviderName());
        }
    }
}
