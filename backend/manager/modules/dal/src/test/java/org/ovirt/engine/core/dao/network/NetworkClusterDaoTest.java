package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.ClusterDao;

public class NetworkClusterDaoTest extends BaseDaoTestCase {
    private static final int NETWORK_CLUSTER_COUNT = 4;
    private NetworkClusterDao dao;
    private Cluster cluster;
    private Network network;
    private NetworkCluster newNetworkCluster;
    private Network networkNoCluster;
    private NetworkCluster existingNetworkCluster;
    private Cluster freeCluster;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getNetworkClusterDao();

        ClusterDao clusterDao = dbFacade.getClusterDao();

        cluster = clusterDao.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1"));
        freeCluster = clusterDao.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d3"));

        NetworkDao networkDao = dbFacade.getNetworkDao();

        network = networkDao.getByName("engine");
        networkNoCluster = networkDao.getByName("engine3");

        createNewNetworkCluster();

        existingNetworkCluster = dao.getAll().get(0);
    }

    private void createNewNetworkCluster() {
        newNetworkCluster = new NetworkCluster();
        newNetworkCluster.setNetworkId(networkNoCluster.getId());
        newNetworkCluster.setClusterId(freeCluster.getId());
        newNetworkCluster.setStatus(NetworkStatus.OPERATIONAL);
        newNetworkCluster.setManagement(true);
        newNetworkCluster.setRequired(true);
        newNetworkCluster.setDisplay(true);
        newNetworkCluster.setMigration(true);
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
        List<NetworkCluster> result = dao.getAllForCluster(Guid.newGuid());

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
            assertEquals(cluster.getId(), thiscluster.getClusterId());
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
            assertEquals(network.getId(), cluster.getNetworkId());
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
        assertNetworkClustersEqual(newNetworkCluster, after.get(0));
    }

    private void assertNetworkClustersEqual(NetworkCluster expected, NetworkCluster actual) {
        assertEquals(expected.getClusterId(), actual.getClusterId());
        assertEquals(expected.getNetworkId(), actual.getNetworkId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.isManagement(), actual.isManagement());
        assertEquals(expected.isRequired(), actual.isRequired());
        assertEquals(expected.isMigration(), actual.isMigration());
        assertEquals(expected.isDisplay(), actual.isDisplay());
    }

    /**
     * Ensures that updating a cluster works as expected.
     */
    @Test
    public void testUpdate() {

        existingNetworkCluster.setRequired(!existingNetworkCluster.isRequired());
        existingNetworkCluster.setDisplay(!existingNetworkCluster.isDisplay());
        existingNetworkCluster.setMigration(!existingNetworkCluster.isMigration());
        existingNetworkCluster.setManagement(!existingNetworkCluster.isManagement());
        existingNetworkCluster.setStatus(invert(existingNetworkCluster.getStatus()));

        dao.update(existingNetworkCluster);

        NetworkCluster result = dao.get(existingNetworkCluster.getId());

        assertNetworkClustersEqual(existingNetworkCluster, result);
    }

    private NetworkStatus invert(NetworkStatus networkStatus) {
        return networkStatus == NetworkStatus.OPERATIONAL ? NetworkStatus.NON_OPERATIONAL : NetworkStatus.OPERATIONAL;
    }

    /**
     * Ensures that updating a cluster status works as expected.
     */
    @Test
    public void testUpdateStatus() {
        existingNetworkCluster.setStatus(NetworkStatus.NON_OPERATIONAL);

        dao.updateStatus(existingNetworkCluster);

        List<NetworkCluster> result = dao.getAll();
        boolean itworked = false;

        for (NetworkCluster thiscluster : result) {
            itworked |= thiscluster.getClusterId().equals(existingNetworkCluster.getClusterId()) &&
                    thiscluster.getNetworkId().equals(existingNetworkCluster.getNetworkId()) &&
                    (thiscluster.getStatus() == existingNetworkCluster.getStatus());
        }

        assertTrue (itworked);
    }

    /**
     * Ensures that removing a network cluster works.
     */
    @Test
    public void testRemove() {
        int before = dao.getAll().size();

        dao.remove(existingNetworkCluster.getClusterId(), existingNetworkCluster.getNetworkId());

        int after = dao.getAll().size();

        assertEquals(before - 1, after);
    }

    @Test
    public void testSetDisplay() {
        dao.setNetworkExclusivelyAsDisplay(existingNetworkCluster.getClusterId(),
                existingNetworkCluster.getNetworkId());
        List<NetworkCluster> allForCluster = dao.getAllForCluster(existingNetworkCluster.getClusterId());
        for (NetworkCluster net : allForCluster) {
            if (net.getClusterId().equals(existingNetworkCluster.getClusterId())
                    && net.getNetworkId().equals(existingNetworkCluster.getNetworkId())) {
                assertTrue(net.isDisplay());
            } else {
                assertFalse(net.isDisplay());
            }
        }
    }

    @Test
    public void testSetMigration() {
        dao.setNetworkExclusivelyAsMigration(existingNetworkCluster.getClusterId(),
                existingNetworkCluster.getNetworkId());
        List<NetworkCluster> allForCluster = dao.getAllForCluster(existingNetworkCluster.getClusterId());
        for (NetworkCluster net : allForCluster) {
            if (net.getId().equals(existingNetworkCluster.getId())) {
                assertTrue(net.isMigration());
            } else {
                assertFalse(net.isMigration());
            }
        }
    }

    @Test
    public void testSetManagement() {
        dao.setNetworkExclusivelyAsManagement(existingNetworkCluster.getClusterId(),
                existingNetworkCluster.getNetworkId());
        List<NetworkCluster> allForCluster = dao.getAllForCluster(existingNetworkCluster.getClusterId());
        for (NetworkCluster net : allForCluster) {
            if (net.getId().equals(existingNetworkCluster.getId())) {
                assertTrue(net.isManagement());
            } else {
                assertFalse(net.isManagement());
            }
        }
    }

    @Test
    public void testSetGluster() {
        dao.setNetworkExclusivelyAsGluster(existingNetworkCluster.getClusterId(),
                existingNetworkCluster.getNetworkId());
        List<NetworkCluster> allForCluster = dao.getAllForCluster(existingNetworkCluster.getClusterId());
        for (NetworkCluster net : allForCluster) {
            if (net.getId().equals(existingNetworkCluster.getId())) {
                assertTrue(net.isGluster());
            } else {
                assertFalse(net.isGluster());
            }
        }
    }

    @Test
    public void testSetGlusterAsNull() {
        dao.setNetworkExclusivelyAsGluster(existingNetworkCluster.getClusterId(), null);
        List<NetworkCluster> allForCluster = dao.getAllForCluster(existingNetworkCluster.getClusterId());
        for (NetworkCluster net : allForCluster) {
            assertFalse(net.isGluster());
        }
    }
}
