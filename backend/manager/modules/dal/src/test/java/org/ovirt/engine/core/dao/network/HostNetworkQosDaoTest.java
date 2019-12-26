package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class HostNetworkQosDaoTest extends BaseGenericDaoTestCase<Guid, HostNetworkQos, HostNetworkQosDao> {
    @Override
    protected HostNetworkQos generateNewEntity() {
        HostNetworkQos newQos = new HostNetworkQos();
        newQos.setId(new Guid("de956031-6be2-43d6-bb90-5191c9253321"));
        newQos.setName("host_network_qos_d");
        newQos.setStoragePoolId(FixturesTool.STORAGE_POOL_NO_DOMAINS);
        newQos.setOutAverageLinkshare(1000);
        newQos.setOutAverageUpperlimit(2000);
        newQos.setOutAverageRealtime(500);
        return newQos;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setName("host_network_qos_b");
        existingEntity.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        existingEntity.setOutAverageLinkshare(1000);
        existingEntity.setOutAverageUpperlimit(2000);
        existingEntity.setOutAverageRealtime(500);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.HOST_NETWORK_QOS_B;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 4;
    }

    /**
     * Ensures that the number of QoS entities returned for each data center is consistent.
     */
    @Test
    public void testGetAllForDc() {
        assertEquals(3, dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES).size());
        assertTrue(dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_NFS).isEmpty());
    }
}
