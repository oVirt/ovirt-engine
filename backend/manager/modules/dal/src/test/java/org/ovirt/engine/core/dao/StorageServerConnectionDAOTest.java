package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionDAOTest extends BaseDAOTestCase {
    private static final int SERVER_CONNECTION_COUNT_FOR_SPECIFIC_STORAGE = 7;
    private static final String EXISTING_DOMAIN_STORAGE_NAME = "G95OWd-Wvck-vftu-pMq9-9SAC-NF3E-ulDPsQ";
    private static final Guid EXISTING_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

    private StorageServerConnectionDAO dao;
    private StorageServerConnections newServerConnection;
    private StorageServerConnections existingConnection;
    private StorageServerConnections existingNfsAutoConnection;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageServerConnectionDao();

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
     * @return
     */
    @Test
    public void testgetAllConnectableStorageSeverConnectionWithNoConnections() {
        List<StorageServerConnections> result = dao.getAllConnectableStorageSeverConnection(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a set of records are returned.
     */
    @Test
    public void testgetAllConnectableStorageSeverConnectionl() {
        List<StorageServerConnections> result = dao.getAllConnectableStorageSeverConnection(EXISTING_STORAGE_POOL_ID);

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
        StorageServerConnections conn = dao.get(newServerConnection.getid());
        assertNull(conn);

        dao.save(newServerConnection);

        conn = dao.get(newServerConnection.getid());

        assertEquals(newServerConnection, conn);
    }

    /**
     * Ensures updating a connection works as expected.
     */
    @Test
    public void testUpdateIscsiServerConnection() {
        existingConnection.setiqn("1.2.3.4");

        dao.update(existingConnection);

        StorageServerConnections result = dao.get(existingConnection.getid());

        assertEquals(existingConnection, result);
    }

    @Test
    public void testUpdateNfsServerConnection() {
        //create a new connection
        StorageServerConnections newNFSServerConnection = new StorageServerConnections();
        newNFSServerConnection.setid("0cb136e8-e5ed-472b-8914-260bc48c2987");
        newNFSServerConnection.setstorage_type(StorageType.NFS);
        newNFSServerConnection.setconnection("host/lib/data");
        newNFSServerConnection.setNfsVersion(NfsVersion.V4);
        newNFSServerConnection.setNfsRetrans((short) 0);
        dao.save(newNFSServerConnection);

        //get it from db
        StorageServerConnections newNFSServerConnectionFromDB = dao.get("0cb136e8-e5ed-472b-8914-260bc48c2987");

        //update its properties and save back to db (update)
        newNFSServerConnectionFromDB.setconnection("/host2/lib/data");
        newNFSServerConnectionFromDB.setNfsRetrans((short) 3);
        newNFSServerConnectionFromDB.setNfsTimeo((short)100);
        dao.update(newNFSServerConnectionFromDB);

        //get it again after the update
        StorageServerConnections updatedNFSServerConnectionFromDB = dao.get("0cb136e8-e5ed-472b-8914-260bc48c2987");
        assertEquals(updatedNFSServerConnectionFromDB.getconnection(),newNFSServerConnectionFromDB.getconnection());
        assertEquals(updatedNFSServerConnectionFromDB.getid(),newNFSServerConnectionFromDB.getid());
        assertEquals(updatedNFSServerConnectionFromDB.getNfsRetrans(),newNFSServerConnectionFromDB.getNfsRetrans());
        assertEquals(updatedNFSServerConnectionFromDB.getNfsTimeo(),newNFSServerConnectionFromDB.getNfsTimeo());
        assertNotSame(newNFSServerConnection.getconnection(), updatedNFSServerConnectionFromDB.getconnection());
        //cleanup...
        dao.remove("0cb136e8-e5ed-472b-8914-260bc48c2987");
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

    @Test
    public void testGetAllConnectionsOfNfsDomain() {
      List<StorageServerConnections> connections = dao.getAllForDomain(Guid.createGuidFromString("d9ede37f-e6c3-4bf9-a984-19174070aa31"));
      assertEquals(connections.size(),1);
      assertEquals(connections.get(0).getid(),"0cc146e8-e5ed-482c-8814-270bc48c2981");
    }

    @Test
    public void testGetAllConnectionsOfIscsiDomain() {
      List<StorageServerConnections> connections = dao.getAllForDomain(Guid.createGuidFromString("72e3a666-89e1-4005-a7ca-f7548004a9ab"));
      assertEquals(connections.size(),2);
      assertTrue((connections.get(0).getid().equals("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk")) || (connections.get(0).getid().equals("0cc146e8-e5ed-482c-8814-270bc48c297e")));
    }
}
