package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class NetworkDaoTest extends BaseDaoTestCase {
    private static final Guid MANAGEMENT_NETWORK_ID = new Guid("58d5c1c6-cb15-4832-b2a4-1234567890ab");

    private NetworkDao dao;
    private Guid cluster;
    private Guid datacenter;
    private Network new_net;
    private static final String EXISTING_NETWORK_NAME1 = "engine";
    private static final String EXISTING_NETWORK_NAME2 = "engine3";
    private static final int NUM_OF_NETWORKS = 7;
    private static final int NUM_OF_MANAGEMENT_NETWORKS = 1;
    private static final String NETWORK_LABEL = "lbl1";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getNetworkDao();

        cluster = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
        datacenter = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

        new_net = new Network();
        new_net.setName("newnet1");
        new_net.setDescription("New network");
        new_net.setDataCenterId(datacenter);
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
        Network result = dao.getByName(EXISTING_NETWORK_NAME1);

        assertNotNull(result);
        assertEquals(EXISTING_NETWORK_NAME1, result.getName());
    }

    /**
     * Ensures that retrieving a network by name and data center works as expected.
     */
    @Test
    public void testGetByNameAndDataCenter() {
        Network result = dao.getByNameAndDataCenter(EXISTING_NETWORK_NAME1, datacenter);

        assertNotNull(result);
        assertEquals(EXISTING_NETWORK_NAME1, result.getName());
    }

    /**
     * Ensures that retrieving a network by name and cluster works as expected.
     */
    @Test
    public void testGetByNameAndCluster() {
        Network result = dao.getByNameAndCluster(EXISTING_NETWORK_NAME1, cluster);

        assertNotNull(result);
        assertEquals(EXISTING_NETWORK_NAME1, result.getName());
    }

    /**
     * Ensures that retrieving the management network by cluster works as expected.
     */
    @Test
    public void testGetManagementNetworkByCluster() {
        Network result = dao.getManagementNetwork(cluster);

        assertNotNull(result);
        assertEquals(MANAGEMENT_NETWORK_ID, result.getId());
    }

    /**
     * Ensures that all management networks are returned.
     */
    @Test
    public void testGetManagementNetworks() {
        List<Network> result = dao.getManagementNetworks(datacenter);

        assertEquals(NUM_OF_MANAGEMENT_NETWORKS, result.size());
    }

    /**
     * Ensures that all networks are returned.
     */
    @Test
    public void testGetAll() {
        List<Network> result = dao.getAll();

        assertNotNull(result);
        assertEquals(NUM_OF_NETWORKS, result.size());
    }

    /**
     * Ensures that all networks are returned for a specific user when filter is on.
     */
    @Test
    public void testFilteredGetAll() {
        List<Network> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that no networks is returned for a specific user when filter is on.
     */
    @Test
    public void testFilteredGetAllWithNoPermissions() {
        List<Network> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all networks are returned for a unprivileged user when filter is off.
     */
    @Test
    public void testUnfilteredGetAllWithNoPermissions() {
        List<Network> result = dao.getAll(UNPRIVILEGED_USER_ID, false);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned when the specified cluster has no networks.
     */
    @Test
    public void testGetAllForClusterWithInvalidCluster() {
        List<Network> result = dao.getAllForCluster(Guid.newGuid());

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
     * Asserts the result of {@link NetworkDao#getAllForCluster(Guid)} contains all the required networks
     */
    private static void assertGetAllForClusterResult(List<Network> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertIsSorted(result);
    }

    private static void assertIsSorted(List<Network> result) {
        Network previous = null;
        for (Network network : result) {
            if (previous != null && network.getName().compareTo(previous.getName()) < 0) {
                fail(String.format("List of networks is not ordered by network name, %s came before %s.",
                        previous.getName(),
                        network.getName()));
            }
            previous = network;
        }
    }

    /**
     * Ensures that an empty collection is returned when the data center has no networks.
     */
    @Test
    public void testGetAllForDataCenterWithInvalidDataCenter() {
        List<Network> result = dao.getAllForDataCenter(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of networks are returned for the given data center.
     */
    @Test
    public void testGetAllForDataCenter() {
        List<Network> result = dao.getAllForDataCenter(datacenter);
        verifyDataCenterNetworks(result);
    }

    /**
     * Ensures that the right set of networks are returned for the given data center for a specific user according to
     * the filter.
     */
    @Test
    public void testFilteredGetAllForDataCenter() {
        List<Network> result = dao.getAllForDataCenter(datacenter, PRIVILEGED_USER_ID, true);
        verifyDataCenterNetworks(result);
    }

    /**
     * Ensures that the no network is returned for the given data center for a user with no permissions on network
     * entities
     */
    @Test
    public void testFilteredGetAllForDataCenterWithNoPermissions() {
        List<Network> result = dao.getAllForDataCenter(datacenter, UNPRIVILEGED_USER_ID, true);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the all networks are returned for the given data center for a specific user when the filter is off.
     */
    @Test
    public void testUnFilteredGetAllForDataCenterWithNoPermissions() {
        List<Network> result = dao.getAllForDataCenter(datacenter, PRIVILEGED_USER_ID, false);
        verifyDataCenterNetworks(result);
    }

    private void verifyDataCenterNetworks(List<Network> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Network net : result) {
            assertEquals(datacenter, net.getDataCenterId());
        }
    }

    /**
     * Ensures that the correct networks are returned for a given QoS ID.
     */
    @Test
    public void testGetAllForValidQos() {
        List<Network> result = dao.getAllForQos(FixturesTool.HOST_NETWORK_QOS_A);
        assertNotNull(result);
        assertTrue(result.size() == 1);
        assertEquals(result.get(0).getId(), FixturesTool.NETWORK_ENGINE);
    }

    /**
     * Ensures that an empty collection is returned for a QoS that no network is using.
     */
    @Test
    public void testGetAllForUnusedQos() {
        List<Network> result = dao.getAllForQos(FixturesTool.HOST_NETWORK_QOS_B);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of networks are returned for the given provider.
     */
    @Test
    public void testGetAllForProvider() {
        List<Network> result = dao.getAllForProvider(FixturesTool.PROVIDER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Network network : result) {
            assertEquals(FixturesTool.PROVIDER_ID, network.getProvidedBy().getProviderId());
        }
    }

    /**
     * Ensures a list of networks labels is returned for a given data-center
     */
    @Test
    public void testGetAllNetworkLabelsForDataCenter() {
        Set<String> result = dao.getAllNetworkLabelsForDataCenter(datacenter);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures a list of networks is returned for a given cluster by a specific label
     */
    @Test
    public void getAllByLabelForCluster() {
        List<Network> result = dao.getAllByLabelForCluster(NETWORK_LABEL, cluster);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (Network network : result) {
            assertEquals(NETWORK_LABEL, network.getLabel());
        }
    }

    /**
     * Ensures that saving a network works as expected.
     */
    @Test
    public void testSave() {
        List<NetworkCluster> clustersFromDB = dbFacade.getNetworkClusterDao().getAllForCluster(cluster);
        NetworkCluster clusterFromDB = clustersFromDB.get(0);
        assertNotNull(clusterFromDB);
        new_net.setCluster(clusterFromDB);
        new_net.setId(Guid.newGuid());
        dao.save(new_net);

        Network result = dao.getByName(new_net.getName());

        assertNotNull(result);
        assertEquals(new_net, result);
    }

    /**
     * Ensures updating a network works as expected.
     */
    @Test
    public void testUpdate() {
        Network before = dao.getByName(EXISTING_NETWORK_NAME1);

        before.setDescription("This is a completely changed description");

        dao.update(before);

        Network after = dao.getByName(EXISTING_NETWORK_NAME1);

        assertNotNull(after);
        assertEquals(before, after);
    }

    /**
     * Ensures that removing a network works as expected.
     */
    @Test
    public void testRemove() {
        Network result = dao.getByName(EXISTING_NETWORK_NAME2);

        assertNotNull(result);

        dao.remove(result.getId());

        result = dao.getByName(EXISTING_NETWORK_NAME2);

        assertNull(result);
    }
}
