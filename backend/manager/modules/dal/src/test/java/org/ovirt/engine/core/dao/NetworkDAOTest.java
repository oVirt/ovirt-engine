package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.compat.Guid;

public class NetworkDAOTest extends BaseDAOTestCase {
    private NetworkDAO dao;
    private Guid cluster;
    private Guid datacenter;
    private Network new_net;
    private String existing_net_name;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getNetworkDao());

        cluster = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
        datacenter = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

        existing_net_name = "engine";
        new_net = new Network();
        new_net.setname("newnet1");
        new_net.setdescription("New network");
        new_net.setstorage_pool_id(datacenter);
    }

    /**
     * Ensures that a null network is returned.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        Network result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures that retrieving a network by name works as expected.
     */
    @Test
    public void testGetByName() {
        Network result = dao.getByName("engine");

        assertNotNull(result);
        assertEquals("engine", result.getname());
    }

    /**
     * Ensures that all networks are returned.
     */
    @Test
    public void testGetAll() {
        List<Network> result = dao.getAll();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    /**
     * Ensures that an empty collection is returned when the specified cluster has no networks.
     */
    @Test
    public void testGetAllForClusterWithInvalidCluster() {
        List<Network> result = dao.getAllForCluster(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of networks are returned for the given cluster.
     */
    @Test
    public void testGetAllForCluster() {
        List<Network> result = dao.getAllForCluster(cluster);

        assertGetAllForClusterResult(result);
    }

    /**
     * Ensures the right set of networks are returned for the given cluster,
     * with a privileged user
     */
    @Test
    public void testGetAllForClusterFilteredWithPermissions() {
        // A use with permissions
        List<Network> result = dao.getAllForCluster(cluster, PRIVILEGED_USER_ID, true);

        assertGetAllForClusterResult(result);
    }

    /**
     * Ensures the right set of networks are returned for the given cluster,
     * with a unprivileged user and with filtering enabled
     */
    @Test
    public void testGetAllForClusterFilteredWithPermissionsNoPermissions() {
        // A use with permissions
        List<Network> result = dao.getAllForCluster(cluster, UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of networks are returned for the given cluster,
     * with a unprivileged user, but with no filtering
     */
    @Test
    public void testGetAllForClusterFilteredWithPermissionsNoPermissionsAndNoFilter() {
        // A use with permissions
        List<Network> result = dao.getAllForCluster(cluster, UNPRIVILEGED_USER_ID, false);

        assertGetAllForClusterResult(result);
    }

    /**
     * Asserts the result of {@link NetworkDAO#getAllForCluster(Guid)} contains all the required networks
     * @param result
     */
    private static void assertGetAllForClusterResult(List<Network> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned when the data center has no networks.
     */
    @Test
    public void testGetAllForDataCenterWithInvalidDataCenter() {
        List<Network> result = dao.getAllForDataCenter(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of networks are returned for the given data center.
     */
    @Test
    public void testGetAllForDataCenter() {
        List<Network> result = dao.getAllForDataCenter(datacenter);

        assertGetAllForClusterResult(result);
        for (Network net : result) {
            assertEquals(datacenter, net.getstorage_pool_id());
        }
    }

    /**
     * Ensures that saving a network works as expected.
     */
    @Test
    public void testSave() {
        List<network_cluster> clustersFromDB = dbFacade.getNetworkClusterDao().getAllForCluster(cluster);
        network_cluster clusterFromDB = clustersFromDB.get(0);
        assertNotNull(clusterFromDB);
        new_net.setCluster(clusterFromDB);
        new_net.setId(Guid.NewGuid());
        dao.save(new_net);

        Network result = dao.getByName(new_net.getname());

        assertNotNull(result);
        assertEquals(new_net, result);
    }

    /**
     * Ensures updating a network works as expected.
     */
    @Test
    public void testUpdate() {
        Network before = dao.getByName(existing_net_name);

        before.setdescription("This is a completely changed description");

        dao.update(before);

        Network after = dao.getByName(existing_net_name);

        assertNotNull(after);
        assertEquals(before, after);
    }

    /**
     * Ensures that removing a network works as expected.
     */
    @Test
    public void testRemove() {
        Network result = dao.getByName(existing_net_name);

        assertNotNull(result);

        dao.remove(result.getId());

        result = dao.getByName(existing_net_name);

        assertNull(result);
    }
}
