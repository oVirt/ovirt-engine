package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;
import org.springframework.dao.DuplicateKeyException;

public class NetworkQosDaoTest extends BaseDaoTestCase {

    private NetworkQoSDao dao;

    private static final Guid qosAId = Guid.createGuidFromString("de956031-6be2-43d6-bb90-5191c9253314");
    private static final Guid qosBId = Guid.createGuidFromString("de956031-6be2-43d6-bb90-5191c9253315");
    private static final Guid qosCId = Guid.createGuidFromString("de956031-6be2-43d6-bb90-5191c9253316");
    private static final Guid qosDId = Guid.createGuidFromString("de956031-6be2-43d6-bb90-5191c9253317");


    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = getDbFacade().getNetworkQosDao();
    }
    /**
     * Ensures that retrieving with an invalid ID returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        NetworkQoS result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving VDS by ID works as expected.
     */
    @Test
    public void testGetNetworkQos() {
        NetworkQoS result = dao.get(qosAId);
        NetworkQoS trueA = new NetworkQoS();
        trueA.setId(qosAId);
        trueA.setName("network_qos_a");
        trueA.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        trueA.setInboundAverage(1000);
        trueA.setInboundPeak(2000);
        trueA.setInboundBurst(500);
        trueA.setOutboundAverage(1000);
        trueA.setOutboundPeak(2000);
        trueA.setOutboundBurst(500);

        assertNotNull(result);
        assertTrue(trueA.equals(result));
    }

    /**
     * Test getAll
     */
    @Test
    public void testGetAllNetworkQos() {
        assertTrue(dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES).size() == 2);
    }

    /**
     * test update
     */
    @Test
    public void testUpdateNetworkQos() {
        NetworkQoS newB = new NetworkQoS();
        newB.setId(qosBId);
        newB.setName("newB");
        newB.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        newB.setInboundAverage(30);
        newB.setInboundPeak(30);
        newB.setInboundBurst(30);
        newB.setOutboundAverage(30);
        newB.setOutboundPeak(30);
        newB.setOutboundBurst(30);

        dao.update(newB);

        NetworkQoS afterUpdate = dao.get(qosBId);
        assertTrue(newB.equals(afterUpdate));
    }

    /**
     * test remove
     */
    @Test
    public void testRemoveNetworkQos() {
        dao.remove(qosCId);
        NetworkQoS afterRemove = dao.get(qosCId);
        assertNull(afterRemove);
    }

    /**
     * test save
     */
    @Test
    public void testSaveNetworkQos() {
        NetworkQoS qosD = new NetworkQoS();
        qosD.setId(qosDId);
        qosD.setName("qos_d");
        qosD.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        qosD.setInboundAverage(200);
        qosD.setInboundPeak(200);
        qosD.setInboundBurst(200);
        qosD.setOutboundAverage(200);
        qosD.setOutboundPeak(200);
        qosD.setOutboundBurst(200);

        dao.save(qosD);
        NetworkQoS returnedD = dao.get(qosDId);
        assertTrue(qosD.equals(returnedD));
    }

    /**
     * Test getAllForStoragePool
     */
    @Test
    public void testGetAllNetworkQosForStoragePool() {
        assertTrue(dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES).size() == 2);
    }

    @Test
    public void testCheckNullNameNotUnique() {
        checkNameUniquness(null);
    }

    @Test(expected = DuplicateKeyException.class)
    public void testCheckNameUniquness() {
        checkNameUniquness("SomeName");
    }

    public void checkNameUniquness(String name) {
        NetworkQoS entity = new NetworkQoS();
        entity.setId(Guid.newGuid());
        entity.setName(name);
        entity.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        dao.save(entity);
        entity.setId(Guid.newGuid());
        dao.save(entity);
    }
}
