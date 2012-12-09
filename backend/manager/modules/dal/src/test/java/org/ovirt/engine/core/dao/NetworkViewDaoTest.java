package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NetworkView;

public class NetworkViewDaoTest extends BaseDAOTestCase {
    private NetworkViewDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = prepareDAO(dbFacade.getNetworkViewDao());
    }

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
}
