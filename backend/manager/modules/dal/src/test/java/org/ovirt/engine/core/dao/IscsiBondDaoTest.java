package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.compat.Guid;

public class IscsiBondDaoTest extends BaseGenericDaoTestCase<Guid, IscsiBond, IscsiBondDao> {
    @Override
    protected IscsiBond generateNewEntity() {
        IscsiBond newIscsiBond = new IscsiBond();
        newIscsiBond.setId(Guid.newGuid());
        newIscsiBond.setName("Multipath");
        newIscsiBond.setDescription("New iscsi bond for multipathing");
        newIscsiBond.setStoragePoolId(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        return newIscsiBond;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setDescription("10GB iscsi bond");
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.ISCSI_BOND_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 2;
    }

    @Test
    public void testGetAllByStoragePoolId() {
        List<IscsiBond> iscsiBonds = dao.getAllByStoragePoolId(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertEquals(1, iscsiBonds.size());
    }

    @Test
    public void testGetAllByStoragePoolIdStoragePoolDoesNotExist() {
        List<IscsiBond> iscsiBonds = dao.getAllByStoragePoolId(Guid.Empty);
        assertTrue(iscsiBonds.isEmpty());
    }

    @Test
    public void testGetNetworkIdsByIscsiBondId() {
        List<Guid> networkIds = dao.getNetworkIdsByIscsiBondId(Guid.newGuid());
        assertTrue(networkIds.isEmpty());
    }

    @Override
    @Test
    public void testRemove() {
        super.testRemove();
        List<Guid> networks = dao.getNetworkIdsByIscsiBondId(getExistingEntityId());
        List<String> connections = dao.getStorageConnectionIdsByIscsiBondId(getExistingEntityId());

        assertTrue(networks.isEmpty());
        assertTrue(connections.isEmpty());
    }

    @Test
    public void testAddNetworkToIscsiBond() {
        dao.addNetworkToIscsiBond(FixturesTool.ISCSI_BOND_ID, FixturesTool.NETWORK_ENGINE_2);

        List<Guid> networks = dao.getNetworkIdsByIscsiBondId(FixturesTool.ISCSI_BOND_ID);
        assertEquals(2, networks.size());
        assertTrue(networks.contains(FixturesTool.NETWORK_ENGINE_2));
    }

    @Test
    public void testGetIscsiBondIdByNetworkId() {
        List<IscsiBond> fetchedIscsiBonds = dao.getIscsiBondsByNetworkId(FixturesTool.NETWORK_ENGINE);
        assertEquals(1, fetchedIscsiBonds.size());
        assertEquals(FixturesTool.ISCSI_BOND_ID, fetchedIscsiBonds.get(0).getId());
    }

    @Test
    public void testGetEmptyIscsiBondIdByNetworkId() {
        List<IscsiBond> fetchedIscsiBonds = dao.getIscsiBondsByNetworkId(FixturesTool.NETWORK_ENGINE_2);
        assertEquals(0, fetchedIscsiBonds.size());
    }

    @Test
    public void testGetEmptyIscsiBondIdByNotExistingNetworkId() {
        List<IscsiBond> fetchedIscsiBonds = dao.getIscsiBondsByNetworkId(Guid.Empty);
        assertEquals(0, fetchedIscsiBonds.size());
    }

    @Test
    public void testRemoveNetworkFromIscsiBond() {
        List<Guid> networks = dao.getNetworkIdsByIscsiBondId(FixturesTool.ISCSI_BOND_ID);
        Guid networkId = networks.get(0);

        dao.removeNetworkFromIscsiBond(FixturesTool.ISCSI_BOND_ID, networkId);

        networks = dao.getNetworkIdsByIscsiBondId(FixturesTool.ISCSI_BOND_ID);
        assertTrue(networks.isEmpty());
    }

    @Test
    public void testAddStorageConnectionToIscsiBond() {
        dao.addStorageConnectionToIscsiBond(FixturesTool.ISCSI_BOND_ID, FixturesTool.STORAGE_CONNECTION_ID);

        List<String> connections = dao.getStorageConnectionIdsByIscsiBondId(FixturesTool.ISCSI_BOND_ID);
        assertEquals(3, connections.size());
        assertTrue(connections.contains(FixturesTool.STORAGE_CONNECTION_ID));
    }

    @Test
    public void testRemoveStorageConnectionFromIscsiBond() {
        List<String> connections = dao.getStorageConnectionIdsByIscsiBondId(FixturesTool.ISCSI_BOND_ID);
        String connectionId = connections.get(0);

        dao.removeStorageConnectionFromIscsiBond(FixturesTool.ISCSI_BOND_ID, connectionId);

        connections = dao.getStorageConnectionIdsByIscsiBondId(FixturesTool.ISCSI_BOND_ID);
        assertEquals(1, connections.size());
        assertFalse(connections.contains(connectionId));
    }
}
