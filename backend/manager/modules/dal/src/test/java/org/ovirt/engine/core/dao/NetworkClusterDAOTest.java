package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;

public class NetworkClusterDAOTest extends BaseDAOTestCase {
    private static final int NETWORK_CLUSTER_COUNT = 3;
    private NetworkClusterDAO dao;
    private VDSGroup cluster;
    private Network network;
    private NetworkCluster newNetworkCluster;
    private Network networkNoCluster;
    private NetworkCluster existingNetworkCluster;
    private VDSGroup freeCluster;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getNetworkClusterDao());

        VdsGroupDAO vdsGroupDAO = prepareDAO(dbFacade.getVdsGroupDao());

        cluster = vdsGroupDAO.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1"));
        freeCluster = vdsGroupDAO.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d3"));

        NetworkDAO networkDAO = prepareDAO(dbFacade.getNetworkDao());

        network = networkDAO.getByName("engine");
        networkNoCluster = networkDAO.getByName("engine3");

        newNetworkCluster = new NetworkCluster();
        newNetworkCluster.setnetwork_id(networkNoCluster.getId());
        newNetworkCluster.setcluster_id(freeCluster.getId());

        existingNetworkCluster = dao.getAll().get(0);
    }

    /**
     * Ensures that retrieving an instance works as expected.
     */
    @Test
    public void testGet() {
        assertEquals(existingNetworkCluster, dao.get(existingNetworkCluster.getId()));
    }

    /**
     * Ensures that retrieving all instances works as expected.
     */
    @Test
    public void testGetAll() {
        List<NetworkCluster> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NETWORK_CLUSTER_COUNT, result.size());
    }

    /**
     * Ensures that an empty collection is returned when the cluster specified doesn't have any networks.
     */
    @Test
    public void testGetAllForClusterWithInvalidCluster() {
        List<NetworkCluster> result = dao.getAllForCluster(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that retrieving all for a specific cluster works as expected.
     */
    @Test
    public void testGetAllForCluster() {
        List<NetworkCluster> result = dao.getAllForCluster(cluster.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (NetworkCluster thiscluster : result) {
            assertEquals(cluster.getId(), thiscluster.getcluster_id());
        }
    }

    /**
     * Ensures that an empty collection is returned if the network has no clusters.
     */
    @Test
    public void testGetAllForNetworkWithInvalidNetwork() {
        List<NetworkCluster> result = dao.getAllForNetwork(networkNoCluster.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set is returned for the specified network.
     */
    @Test
    public void testGetAllForNetwork() {
        List<NetworkCluster> result = dao.getAllForNetwork(network.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (NetworkCluster cluster : result) {
            assertEquals(network.getId(), cluster.getnetwork_id());
        }
    }

    /**
     * Ensures that saving a cluster works as expected.
     */
    @Test
    public void testSave() {
        List<NetworkCluster> before = dao.getAllForNetwork(networkNoCluster.getId());

        // ensure that we have nothing to start
        assertTrue(before.isEmpty());

        dao.save(newNetworkCluster);

        List<NetworkCluster> after = dao.getAllForNetwork(networkNoCluster.getId());

        assertFalse(after.isEmpty());
        assertEquals(newNetworkCluster, after.get(0));
    }

    /**
     * Ensures that updating a cluster works as expected.
     */
    @Test
    public void testUpdate() {
        existingNetworkCluster.setRequired(!existingNetworkCluster.isRequired());

        dao.update(existingNetworkCluster);

        List<NetworkCluster> result = dao.getAll();
        boolean itworked = false;

        for (NetworkCluster thiscluster : result) {
            itworked |= (thiscluster.getcluster_id().equals(existingNetworkCluster.getcluster_id())) &&
                    (thiscluster.getnetwork_id().equals(existingNetworkCluster.getnetwork_id())) &&
                    (thiscluster.getstatus() == existingNetworkCluster.getstatus());
        }

        assert (itworked);
    }

    /**
     * Ensures that updating a cluster status works as expected.
     */
    @Test
    public void testUpdateStatus() {
        existingNetworkCluster.setstatus(NetworkStatus.NON_OPERATIONAL);

        dao.updateStatus(existingNetworkCluster);

        List<NetworkCluster> result = dao.getAll();
        boolean itworked = false;

        for (NetworkCluster thiscluster : result) {
            itworked |= (thiscluster.getcluster_id().equals(existingNetworkCluster.getcluster_id())) &&
            (thiscluster.getnetwork_id().equals(existingNetworkCluster.getnetwork_id())) &&
            (thiscluster.getstatus() == existingNetworkCluster.getstatus());
        }

        assert (itworked);
    }

    /**
     * Ensures that removing a network cluster works.
     */
    @Test
    public void testRemove() {
        int before = dao.getAll().size();

        dao.remove(existingNetworkCluster.getcluster_id(), existingNetworkCluster.getnetwork_id());

        int after = dao.getAll().size();

        assertEquals(before - 1, after);
    }

    @Test
    public void testSetDisplay() {
        dao.setNetworkExclusivelyAsDisplay(existingNetworkCluster.getcluster_id(),
                existingNetworkCluster.getnetwork_id());
        List<NetworkCluster> allForCluster = dao.getAllForCluster(existingNetworkCluster.getcluster_id());
        for (NetworkCluster net : allForCluster) {
            if (net.getcluster_id().equals(existingNetworkCluster.getcluster_id())
                    && net.getnetwork_id().equals(existingNetworkCluster.getnetwork_id())) {
                assertTrue(net.getis_display());
            } else {
                assertFalse(net.getis_display());
            }
        }
    }
}
