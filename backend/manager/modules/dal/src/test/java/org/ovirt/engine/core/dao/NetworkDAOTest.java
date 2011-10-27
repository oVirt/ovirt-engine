package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.compat.Guid;

public class NetworkDAOTest extends BaseDAOTestCase {
    private NetworkDAO dao;
    private Guid cluster;
    private Guid datacenter;
    private network new_net;
    private String existing_net_name;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getNetworkDAO());

        cluster = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
        datacenter = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

        existing_net_name = "engine";
        new_net = new network();
        new_net.setname("newnet1");
        new_net.setdescription("New network");
        new_net.setstorage_pool_id(datacenter);
    }

    /**
     * Ensures that a null network is returned.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        network result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures that retrieving a network by name works as expected.
     */
    @Test
    public void testGetByName() {
        network result = dao.getByName("engine");

        assertNotNull(result);
        assertEquals("engine", result.getname());
    }

    /**
     * Ensures that all networks are returned.
     */
    @Test
    public void testGetAll() {
        List<network> result = dao.getAll();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    /**
     * Ensures that an empty collection is returned when the specified cluster has no networks.
     */
    @Test
    public void testGetAllForClusterWithInvalidCluster() {
        List<network> result = dao.getAllForCluster(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of networks are returved for the given cluster.
     */
    @Test
    public void testGetAllForCluster() {
        List<network> result = dao.getAllForCluster(cluster);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned when the data center has no networks.
     */
    @Test
    public void testGetAllForDataCenterWithInvalidDataCenter() {
        List<network> result = dao.getAllForDataCenter(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of networks are returned for the given data center.
     */
    @Test
    public void testGetAllForDataCenter() {
        List<network> result = dao.getAllForDataCenter(datacenter);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (network net : result) {
            assertEquals(datacenter, net.getstorage_pool_id());
        }
    }

    /**
     * Ensures that saving a network works as expected.
     */
    @Test
    public void testSave() {
        List<network_cluster> clustersFromDB = dbFacade.getNetworkClusterDAO().getAllForCluster(cluster);
        network_cluster clusterFromDB = clustersFromDB.get(0);
        assertNotNull(clusterFromDB);
        new_net.setCluster(clusterFromDB);
        dao.save(new_net);

        network result = dao.getByName(new_net.getname());

        assertNotNull(result);
        assertEquals(new_net, result);
    }

    /**
     * Ensures updating a network works as expected.
     */
    @Test
    public void testUpdate() {
        network before = dao.getByName(existing_net_name);

        before.setdescription("This is a completely changed description");

        dao.update(before);

        network after = dao.getByName(existing_net_name);

        assertNotNull(after);
        assertEquals(before, after);
    }

    /**
     * Ensures that removing a network works as expected.
     */
    @Test
    public void testRemove() {
        network result = dao.getByName(existing_net_name);

        assertNotNull(result);

        dao.remove(result.getId());

        result = dao.getByName(existing_net_name);

        assertNull(result);
    }
}
