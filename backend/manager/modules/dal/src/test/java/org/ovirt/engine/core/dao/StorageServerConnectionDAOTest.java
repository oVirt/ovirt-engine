package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionDAOTest extends BaseDAOTestCase {
    private static final int SERVER_CONNECTION_COUNT_FOR_SPECIFIC_STORAGE = 7;
    private static final int SERVER_CONNECTION_COUNT = 9;
    private static final String EXISTING_DOMAIN_STORAGE_NAME = "fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk";
    private static final Guid EXISTING_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

    private StorageServerConnectionDAO dao;
    private StorageServerConnections newServerConnection;
    private StorageServerConnections existingConnection;
    private StorageServerConnections existingNfsAutoConnection;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStorageServerConnectionDao());

        existingConnection = dao.get("0cc146e8-e5ed-482c-8814-270bc48c297f");
        existingNfsAutoConnection = dao.get(FixturesTool.EXISTING_STORAGE_CONNECTION_NFS_AUTO_ID.toString());

        newServerConnection = new StorageServerConnections();
        newServerConnection.setid("0cc146e8-e5ed-482c-8814-270bc48c2980");
        newServerConnection.setconnection(EXISTING_DOMAIN_STORAGE_NAME);
    }

    /**
     * Ensures that null is returned when the id is invalid.
     */
    @Test
    public void testGetServerConnectionWithInvalidId() {
        StorageServerConnections result = dao.get("fakrel");

        assertNull(result);
    }

    /**
     * Ensures retrieving a connection by id works as expected.
     */
    @Test
    public void testGetServerConnection() {
        StorageServerConnections result = dao.get(existingConnection.getid());

        assertNotNull(result);
        assertEquals(existingConnection, result);
    }

    @Test
    public void testGetForIqnWithInvalidIqn() {
        StorageServerConnections result = dao.getForIqn("farkle");

        assertNull(result);
    }

    @Test
    public void testGetForIqn() {
        StorageServerConnections result = dao.getForIqn(existingConnection.getiqn());

        assertNotNull(result);
        assertEquals(existingConnection, result);
    }

    /**
     * Ensures all server connections are returned.
     */
    @Test
    public void testgetAll() {
        List<StorageServerConnections> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(SERVER_CONNECTION_COUNT, result.size());
    }

    /**
     * @return
     */
    @Test
    public void testgetAllForStoragePoolWithNoConnections() {
        List<StorageServerConnections> result = dao.getAllForStoragePool(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a set of records are returned.
     */
    @Test
    public void testgetAllForStoragePool() {
        List<StorageServerConnections> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Retrieves all connections for the given volume group.
     *
     */
    @Test
    public void testgetAllForVolumeGroup() {
        List<StorageServerConnections> result =
                dao.getAllForVolumeGroup(EXISTING_DOMAIN_STORAGE_NAME);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures all the right connections are returned.
     */
    @Test
    public void testgetAllForStorage() {
        List<StorageServerConnections> result = dao.getAllForStorage("10.35.64.25");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(SERVER_CONNECTION_COUNT_FOR_SPECIFIC_STORAGE, result.size());
    }

    /**
     * Ensures saving a connection works as expected.
     */
    @Test
    public void testSaveServerConnection() {
        List<StorageServerConnections> before = dao.getAll();

        dao.save(newServerConnection);

        List<StorageServerConnections> after = dao.getAll();

        assertEquals(before.size() + 1, after.size());
    }

    /**
     * Ensures updating a connection works as expected.
     */
    @Test
    public void testUpdateServerConnection() {
        existingConnection.setiqn("1.2.3.4");

        dao.update(existingConnection);

        StorageServerConnections result = dao.get(existingConnection.getid());

        assertEquals(existingConnection, result);
    }

    /**
     * Ensures removing a connection works as expected.
     */
    @Test
    public void testRemoveServerConnection() {
        dao.remove(existingConnection.getid());

        StorageServerConnections result = dao.get(existingConnection.getid());

        assertNull(result);
    }

    /**
     * Ensures NFS options work as expected.
     */
    @Test
    public void testNfsOptions() {
        newServerConnection.setNfsVersion(NfsVersion.V4);
        newServerConnection.setNfsRetrans((short)5);
        dao.save(newServerConnection);

        StorageServerConnections result = dao.get(newServerConnection.getid());
        assertEquals(result.getNfsVersion(), NfsVersion.V4);
        assertTrue(result.getNfsRetrans() == 5);
        assertNull(result.getNfsTimeo());

        result = dao.get(existingNfsAutoConnection.getid());
        assertEquals(result.getNfsVersion(), NfsVersion.AUTO);
        assertTrue(result.getNfsRetrans() == 7);
        assertTrue(result.getNfsTimeo() == 42);

        result = dao.get(existingConnection.getid());
        assertNull(result.getNfsVersion());
        assertNull(result.getNfsRetrans());
        assertNull(result.getNfsTimeo());
    }

}
