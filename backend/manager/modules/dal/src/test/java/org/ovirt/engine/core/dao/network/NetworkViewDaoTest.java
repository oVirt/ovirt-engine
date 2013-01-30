package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.dao.BaseDAOTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class NetworkViewDaoTest extends BaseDAOTestCase {
    private NetworkViewDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getNetworkViewDao();
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
