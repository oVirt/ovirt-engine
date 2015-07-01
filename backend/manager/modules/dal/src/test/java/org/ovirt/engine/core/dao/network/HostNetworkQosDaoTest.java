package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class HostNetworkQosDaoTest extends BaseDaoTestCase {

    private HostNetworkQosDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = getDbFacade().getHostNetworkQosDao();
    }

    /**
     * Ensures that retrieving with an invalid ID returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        assertNull(dao.get(FixturesTool.NETWORK_ENGINE)); // GUID used by network, not QoS
    }

    /**
     * Ensures that the number of QoS entities returned for each data center is consistent.
     */
    @Test
    public void testGetAllForDc() {
        assertTrue(dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES).size() == 3);
        assertTrue(dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_NFS).isEmpty());
    }

    /**
     * Ensures that retrieving host network QoS by ID works as expected.
     */
    @Test
    public void testGet() {
        HostNetworkQos persistedQos = dao.get(FixturesTool.HOST_NETWORK_QOS_A);

        HostNetworkQos referenceQos = new HostNetworkQos();
        referenceQos.setId(FixturesTool.HOST_NETWORK_QOS_A);
        referenceQos.setName("host_network_qos_a");
        referenceQos.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        referenceQos.setOutAverageLinkshare(1000);
        referenceQos.setOutAverageUpperlimit(2000);
        referenceQos.setOutAverageRealtime(500);

        assertNotNull(persistedQos);
        assertEquals(persistedQos, referenceQos);
    }

    /**
     * Ensures that after an update, the QoS entity is indeed persisted with new values.
     */
    @Test
    public void testUpdate() {
        HostNetworkQos updatedQos = new HostNetworkQos();
        updatedQos.setId(FixturesTool.HOST_NETWORK_QOS_B);
        updatedQos.setName("host_network_qos_b");
        updatedQos.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        updatedQos.setOutAverageLinkshare(1000);
        updatedQos.setOutAverageUpperlimit(2000);
        updatedQos.setOutAverageRealtime(500);

        assertNotEquals(updatedQos, dao.get(FixturesTool.HOST_NETWORK_QOS_B));
        dao.update(updatedQos);
        assertEquals(updatedQos, dao.get(FixturesTool.HOST_NETWORK_QOS_B));
    }

    /**
     * Ensures that a pre-existing host network QoS entity is removed.
     */
    @Test
    public void testRemoveNetworkQos() {
        assertNotNull(dao.get(FixturesTool.HOST_NETWORK_QOS_C));
        dao.remove(FixturesTool.HOST_NETWORK_QOS_C);
        assertNull(dao.get(FixturesTool.HOST_NETWORK_QOS_C));
    }

    /**
     * Ensures that a newly-created host network QoS entity is properly persisted.
     */
    @Test
    public void testSaveNetworkQos() {
        HostNetworkQos newQos = new HostNetworkQos();
        newQos.setId(new Guid("de956031-6be2-43d6-bb90-5191c9253321"));
        newQos.setName("host_network_qos_d");
        newQos.setStoragePoolId(FixturesTool.STORAGE_POOL_NO_DOMAINS);
        newQos.setOutAverageLinkshare(1000);
        newQos.setOutAverageUpperlimit(2000);
        newQos.setOutAverageRealtime(500);

        assertNull(dao.get(newQos.getId()));
        dao.save(newQos);
        assertEquals(newQos, dao.get(newQos.getId()));
    }

}
