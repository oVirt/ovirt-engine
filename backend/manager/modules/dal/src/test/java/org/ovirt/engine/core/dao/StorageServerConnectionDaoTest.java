package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionDaoTest extends BaseDaoTestCase {
    private static final int SERVER_CONNECTION_COUNT_FOR_SPECIFIC_STORAGE = 7;
    private static final String EXISTING_DOMAIN_STORAGE_NAME = "G95OWd-Wvck-vftu-pMq9-9SAC-NF3E-ulDPsQ";
    private static final Guid EXISTING_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

    private StorageServerConnectionDao dao;
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
        newServerConnection.setId("0cc146e8-e5ed-482c-8814-270bc48c2980");
        newServerConnection.setConnection(EXISTING_DOMAIN_STORAGE_NAME);
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
        StorageServerConnections result = dao.get(existingConnection.getId());

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
        StorageServerConnections result = dao.getForIqn(existingConnection.getIqn());

        assertNotNull(result);
        assertEquals(existingConnection, result);
    }

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
    public void testGetAllConnectableStorageSeverConnections() {
        List<StorageServerConnections> result = dao.getAllConnectableStorageSeverConnection(EXISTING_STORAGE_POOL_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetConnectableBlockStorageConnections() {
        List<StorageServerConnections> conns =
                dao.getConnectableStorageConnectionsByStorageType(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER, StorageType.ISCSI);

        assertNotNull(conns);
        assertEquals(2, conns.size());

        for (StorageServerConnections conn : conns) {
            assertEquals(StorageType.ISCSI, conn.getStorageType());
        }
    }

    @Test
    public void testGetConnectableFileStorageConnectionsByStorageType() {
        List<StorageServerConnections> conns =
                dao.getConnectableStorageConnectionsByStorageType(FixturesTool.STORAGE_POOL_MIXED_TYPES, StorageType.NFS);

        assertNotNull(conns);
        assertEquals(1, conns.size());

        for (StorageServerConnections conn : conns) {
            assertEquals(StorageType.NFS, conn.getStorageType());
        }
    }

    @Test
    public void testGetConnectableStorageConnectionsByStorageType() {
        List<StorageServerConnections> result =
                dao.getConnectableStorageConnectionsByStorageType(EXISTING_STORAGE_POOL_ID, null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    @Test
    public void getStorageConnectionsByStorageTypeNoRecordsOfType() {
        List<StorageServerConnections> result =
                dao.getStorageConnectionsByStorageTypeAndStatus(EXISTING_STORAGE_POOL_ID, StorageType.FCP, EnumSet.allOf(StorageDomainStatus.class));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getStorageConnectionsByStorageTypeNoRecordsOfStatus() {
        List<StorageServerConnections> result =
                dao.getStorageConnectionsByStorageTypeAndStatus(EXISTING_STORAGE_POOL_ID, StorageType.NFS, EnumSet.of(StorageDomainStatus.Locked));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getStorageConnectionsByStorageTypeWithRecordsMultipleStatuses() {
        getStorageConnectionsByStorageTypeWithRecords(EnumSet.of(StorageDomainStatus.Maintenance, StorageDomainStatus.Unknown),
                Arrays.asList(FixturesTool.STORAGE_DOAMIN_NFS2_3, FixturesTool.STORAGE_DOAMIN_NFS2_1));
    }

    @Test
    public void getStorageConnectionsByStorageTypeWithRecordsOneStatus() {
        getStorageConnectionsByStorageTypeWithRecords(EnumSet.of(StorageDomainStatus.Maintenance),
                Arrays.asList(FixturesTool.STORAGE_DOAMIN_NFS2_3));
    }


    public void getStorageConnectionsByStorageTypeWithRecords(EnumSet<StorageDomainStatus> statuses,
            Collection<Guid> expectedDomains) {
        List<StoragePoolIsoMap> poolIsoMap =
                dbFacade.getStoragePoolIsoMapDao().getAllForStoragePool(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        List<Guid> storageDomainIds = new LinkedList<>();
        for (StoragePoolIsoMap isoMap : poolIsoMap) {
            if (statuses.contains(isoMap.getStatus())) {
                storageDomainIds.add(isoMap.getStorageId());
            }
        }

        assertTrue("the list of the pool domains expected to be in the given statuses doesn't match the queried data",
                CollectionUtils.isEqualCollection(expectedDomains, storageDomainIds));

        List<StorageServerConnections> result =
                dao.getStorageConnectionsByStorageTypeAndStatus(FixturesTool.STORAGE_POOL_MIXED_TYPES,
                        StorageType.NFS,
                        statuses);

        assertFalse("there should be connections for the tested domains to verify the correctness", result.isEmpty());

        for (StorageServerConnections storageServerConnection : result) {
            assertEquals("connections were loaded with incorrect storage type",
                    StorageType.NFS,
                    storageServerConnection.getStorageType());
        }

        List<StorageServerConnections> domainConnections = new LinkedList<>();
        for (Guid domainId : storageDomainIds) {
            domainConnections.addAll(dao.getAllForDomain(domainId));
        }

        assertTrue("the connections loaded by the given dao function should match the connections loaded separately",
                CollectionUtils.isEqualCollection(domainConnections, result));
    }

    private Set<String> getLunConnections(List<LUNStorageServerConnectionMap> lunConns) {
        Set<String> conns = new HashSet<>();
        for (LUNStorageServerConnectionMap lun_storage_server_connection_map1 : lunConns) {
            conns.add(lun_storage_server_connection_map1.getStorageServerConnection());
        }
        return conns;
    }
    /**
     * Retrieves all connections for the given volume group.
     *
     */
    @Test
    public void testgetAllForVolumeGroup() {
        Set<String> lunConns1 = getLunConnections(dbFacade.getStorageServerConnectionLunMapDao().getAll(FixturesTool.LUN_ID1));
        Set<String> lunConns2 = getLunConnections(dbFacade.getStorageServerConnectionLunMapDao().getAll(FixturesTool.LUN_ID2));
        assertTrue("Both LUNs should have at least one mutual connection",
                CollectionUtils.containsAny(lunConns1, lunConns2));

        List<StorageServerConnections> result =
                dao.getAllForVolumeGroup(EXISTING_DOMAIN_STORAGE_NAME);
        assertFalse(result.isEmpty());
        Set<String> connections  = new HashSet<>();
        for (StorageServerConnections connection : result) {
            assertFalse(connections.contains(connection.getId()));
            connections.add(connection.getId());
        }
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


    @Test
    public void getAllForForConnection() {
        StorageServerConnections conn = dao.get(existingConnection.getId());
        conn.setId("copy");
        dao.save(conn);
        assertGetAllForConnectionResult(Arrays.asList(existingConnection, conn), existingConnection);
    }


    @Test
    public void getAllForForConnectionWithNullValues() {
        StorageServerConnections noNullValConnection =
                createConnection("id1", "connection", null, "username", "password", "portal", "port");
        StorageServerConnections noNullValConnection2 =
                createConnection("id11", "connection", null, "username", "password", "portal", "port");
        assertGetAllForConnectionResult(Arrays.asList(noNullValConnection, noNullValConnection2), noNullValConnection);

        noNullValConnection = createConnection("id2", "connection", "iqn", null, "password", "portal", "port");
        noNullValConnection2 = createConnection("id12", "connection", "iqn", null, "password", "portal", "port");
        assertGetAllForConnectionResult(Arrays.asList(noNullValConnection, noNullValConnection2), noNullValConnection);

        // testing with different passwords to see that it's not being considered as part of the stored procedure.
        noNullValConnection = createConnection("id3", "connection", "iqn", "username", "pass1", "portal", "port");
        noNullValConnection2 = createConnection("id13", "connection", "iqn", "username", "pass2", "portal", "port");
        assertGetAllForConnectionResult(Arrays.asList(noNullValConnection, noNullValConnection2), noNullValConnection);

        noNullValConnection = createConnection("id4", "connection", "iqn", "username", "password", null, "port");
        noNullValConnection2 = createConnection("id14", "connection", "iqn", "username", "password", null, "port");
        assertGetAllForConnectionResult(Arrays.asList(noNullValConnection, noNullValConnection2), noNullValConnection);

        noNullValConnection = createConnection("id5", "connection", "iqn", "username", "password", "portal", null);
        noNullValConnection2 = createConnection("id15", "connection", "iqn", "username", "password", "portal", null);
        assertGetAllForConnectionResult(Arrays.asList(noNullValConnection, noNullValConnection2), noNullValConnection);

        noNullValConnection = createConnection("id6", "b", null, null, null, null, null);
        noNullValConnection2 = createConnection("id16", "b", null, null, null, null, null);
        assertGetAllForConnectionResult(Arrays.asList(noNullValConnection, noNullValConnection2), noNullValConnection);
    }

    private StorageServerConnections createConnection(String id,
            String connection,
            String iqn,
            String username,
            String password,
            String portal,
            String port) {
        StorageServerConnections newConn = new StorageServerConnections();
        newConn.setId(id);
        newConn.setConnection(connection);
        newConn.setIqn(iqn);
        newConn.setPortal(portal);
        newConn.setPort(port);
        newConn.setUserName(username);
        newConn.setPassword(password);
        dao.save(newConn);
        return newConn;
    }

    private void assertGetAllForConnectionResult(List<StorageServerConnections> expected, StorageServerConnections forQuery) {
        assertTrue(CollectionUtils.disjunction(expected, dao.getAllForConnection(forQuery)).isEmpty());
    }

    /**
     * Ensures saving a connection works as expected.
     */
    @Test
    public void testSaveServerConnection() {
        StorageServerConnections conn = dao.get(newServerConnection.getId());
        assertNull(conn);

        dao.save(newServerConnection);

        conn = dao.get(newServerConnection.getId());

        assertEquals(newServerConnection, conn);
    }

    /**
     * Ensures updating a connection works as expected.
     */
    @Test
    public void testUpdateIscsiServerConnection() {
        existingConnection.setIqn("1.2.3.4");

        dao.update(existingConnection);

        StorageServerConnections result = dao.get(existingConnection.getId());

        assertEquals(existingConnection, result);
    }

    @Test
    public void testUpdateNfsServerConnection() {
        //create a new connection
        StorageServerConnections newNFSServerConnection = new StorageServerConnections();
        newNFSServerConnection.setId("0cb136e8-e5ed-472b-8914-260bc48c2987");
        newNFSServerConnection.setStorageType(StorageType.NFS);
        newNFSServerConnection.setConnection("host/lib/data");
        newNFSServerConnection.setNfsVersion(NfsVersion.V4);
        newNFSServerConnection.setNfsRetrans((short) 0);
        dao.save(newNFSServerConnection);

        //get it from db
        StorageServerConnections newNFSServerConnectionFromDB = dao.get("0cb136e8-e5ed-472b-8914-260bc48c2987");

        //update its properties and save back to db (update)
        newNFSServerConnectionFromDB.setConnection("/host2/lib/data");
        newNFSServerConnectionFromDB.setNfsRetrans((short) 3);
        newNFSServerConnectionFromDB.setNfsTimeo((short)100);
        dao.update(newNFSServerConnectionFromDB);

        //get it again after the update
        StorageServerConnections updatedNFSServerConnectionFromDB = dao.get("0cb136e8-e5ed-472b-8914-260bc48c2987");
        assertEquals(updatedNFSServerConnectionFromDB.getConnection(), newNFSServerConnectionFromDB.getConnection());
        assertEquals(updatedNFSServerConnectionFromDB.getId(), newNFSServerConnectionFromDB.getId());
        assertEquals(updatedNFSServerConnectionFromDB.getNfsRetrans(), newNFSServerConnectionFromDB.getNfsRetrans());
        assertEquals(updatedNFSServerConnectionFromDB.getNfsTimeo(), newNFSServerConnectionFromDB.getNfsTimeo());
        assertNotSame(newNFSServerConnection.getConnection(), updatedNFSServerConnectionFromDB.getConnection());
        //cleanup...
        dao.remove("0cb136e8-e5ed-472b-8914-260bc48c2987");
    }

    /**
     * Ensures removing a connection works as expected.
     */
    @Test
    public void testRemoveServerConnection() {
        dao.remove(existingConnection.getId());

        StorageServerConnections result = dao.get(existingConnection.getId());

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

        StorageServerConnections result = dao.get(newServerConnection.getId());
        assertEquals(result.getNfsVersion(), NfsVersion.V4);
        assertTrue(result.getNfsRetrans() == 5);
        assertNull(result.getNfsTimeo());

        result = dao.get(existingNfsAutoConnection.getId());
        assertEquals(result.getNfsVersion(), NfsVersion.AUTO);
        assertTrue(result.getNfsRetrans() == 7);
        assertTrue(result.getNfsTimeo() == 42);

        result = dao.get(existingConnection.getId());
        assertNull(result.getNfsVersion());
        assertNull(result.getNfsRetrans());
        assertNull(result.getNfsTimeo());
    }

    @Test
    public void testGetAllConnectionsOfNfsDomain() {
      List<StorageServerConnections> connections = dao.getAllForDomain(Guid.createGuidFromString("d9ede37f-e6c3-4bf9-a984-19174070aa31"));
      assertEquals(connections.size(), 1);
      assertEquals(connections.get(0).getId(), "0cc146e8-e5ed-482c-8814-270bc48c2981");
    }

    @Test
    public void testGetAllConnectionsOfIscsiDomain() {
      List<StorageServerConnections> connections = dao.getAllForDomain(Guid.createGuidFromString("72e3a666-89e1-4005-a7ca-f7548004a9ab"));
      assertEquals(connections.size(), 2);
      assertTrue(connections.get(0).getId().equals("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk") || connections.get(0).getId().equals("0cc146e8-e5ed-482c-8814-270bc48c297e"));
    }

    @Test
    public void testGetConnectionsBySpecificIds() {
        List<StorageServerConnections> connections = dao.getByIds(Arrays.asList("0cc146e8-e5ed-482c-8814-270bc48c297f"));
        assertEquals(1, connections.size());
    }
}
