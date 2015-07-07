package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.compat.Guid;

public class IscsiBondDaoTest extends BaseDaoTestCase {

    private IscsiBondDao dao;
    private Guid storagePoolId;
    private IscsiBond newIscsiBond;
    private Guid iscsiBondId;
    private Guid networkId;
    private String connectionId;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getIscsiBondDao();
        storagePoolId = FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER;
        networkId = FixturesTool.NETWORK_ENGINE_2;
        iscsiBondId = FixturesTool.ISCSI_BOND_ID;
        connectionId = FixturesTool.STORAGE_CONNECTION_ID;

        newIscsiBond = new IscsiBond();
        newIscsiBond.setId(Guid.newGuid());
        newIscsiBond.setName("Multipath");
        newIscsiBond.setDescription("New iscsi bond for multipathing");
        newIscsiBond.setStoragePoolId(storagePoolId);
    }

    @Test
    public void testGetAllByStoragePoolId() {
        List<IscsiBond> iscsiBonds = dao.getAllByStoragePoolId(storagePoolId);
        assertEquals(1, iscsiBonds.size());
    }

    @Test
    public void testGetAllByStoragePoolIdStoragePoolDoesNotExist() {
        List<IscsiBond> iscsiBonds = dao.getAllByStoragePoolId(Guid.Empty);
        assertTrue(iscsiBonds.isEmpty());
    }

    @Test
    public void testGetNetworkIdsByIscsiBondId() {
        List<Guid> networkIds = dao.getNetworkIdsByIscsiBondId(newIscsiBond.getId());
        assertTrue(networkIds.isEmpty());
    }

    @Test
    public void testGetIscsiBondByIscsiBondId() {
        IscsiBond iscsiBond = dao.get(iscsiBondId);
        assertNotNull(iscsiBond);
    }

    @Test
    public void testAddNewIscsiBond() {
        dao.save(newIscsiBond);

        IscsiBond iscsiBond = dao.get(newIscsiBond.getId());
        assertEquals(newIscsiBond, iscsiBond);
    }

    @Test
    public void testUpdateIscsiBond() {
        final String newDescription = "10GB iscsi bond";

        IscsiBond iscsiBond = dao.get(iscsiBondId);
        assertFalse(newDescription.equals(iscsiBond.getDescription()));

        iscsiBond.setDescription(newDescription);
        dao.update(iscsiBond);

        iscsiBond = dao.get(iscsiBondId);
        assertEquals(newDescription, iscsiBond.getDescription());
    }

    @Test
    public void testRemoveIscsiBond() {
        IscsiBond iscsiBond = dao.get(iscsiBondId);
        assertNotNull(iscsiBond);

        dao.remove(iscsiBondId);

        iscsiBond = dao.get(iscsiBondId);
        List<Guid> networks = dao.getNetworkIdsByIscsiBondId(iscsiBondId);
        List<String> connections = dao.getStorageConnectionIdsByIscsiBondId(iscsiBondId);

        assertNull(iscsiBond);
        assertTrue(networks.isEmpty());
        assertTrue(connections.isEmpty());
    }

    @Test
    public void testAddNetworkToIscsiBond() {
        dao.addNetworkToIscsiBond(iscsiBondId, networkId);

        List<Guid> networks = dao.getNetworkIdsByIscsiBondId(iscsiBondId);
        assertEquals(2, networks.size());
        assertTrue(networks.contains(networkId));
    }

    @Test
    public void testGetIscsiBondIdByNetworkId() {
        List<IscsiBond> fetchedIscsiBonds = dao.getIscsiBondsByNetworkId(FixturesTool.NETWORK_ENGINE);
        assertEquals(1, fetchedIscsiBonds.size());
        assertEquals(FixturesTool.ISCSI_BOND_ID, fetchedIscsiBonds.get(0).getId());
    }

    @Test
    public void testGetEmptyIscsiBondIdByNetworkId() {
        List<IscsiBond> fetchedIscsiBonds = dao.getIscsiBondsByNetworkId(networkId);
        assertEquals(0, fetchedIscsiBonds.size());
    }

    @Test
    public void testGetEmptyIscsiBondIdByNotExistingNetworkId() {
        List<IscsiBond> fetchedIscsiBonds = dao.getIscsiBondsByNetworkId(Guid.Empty);
        assertEquals(0, fetchedIscsiBonds.size());
    }

    @Test
    public void testRemoveNetworkFromIscsiBond() {
        List<Guid> networks = dao.getNetworkIdsByIscsiBondId(iscsiBondId);
        networkId = networks.get(0);

        dao.removeNetworkFromIscsiBond(iscsiBondId, networkId);

        networks = dao.getNetworkIdsByIscsiBondId(iscsiBondId);
        assertTrue(networks.isEmpty());
    }

    @Test
    public void testAddStorageConnectionToIscsiBond() {
        dao.addStorageConnectionToIscsiBond(iscsiBondId, connectionId);

        List<String> connections = dao.getStorageConnectionIdsByIscsiBondId(iscsiBondId);
        assertEquals(3, connections.size());
        assertTrue(connections.contains(connectionId));
    }

    @Test
    public void testRemoveStorageConnectionFromIscsiBond() {
        List<String> connections = dao.getStorageConnectionIdsByIscsiBondId(iscsiBondId);
        connectionId = connections.get(0);

        dao.removeStorageConnectionFromIscsiBond(iscsiBondId, connectionId);

        connections = dao.getStorageConnectionIdsByIscsiBondId(iscsiBondId);
        assertEquals(1, connections.size());
        assertFalse(connections.contains(connectionId));
    }
}
