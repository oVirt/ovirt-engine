package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionDaoTest
        extends BaseGenericDaoTestCase<String, StorageServerConnections, StorageServerConnectionDao> {

    private static final int SERVER_CONNECTION_COUNT_FOR_SPECIFIC_STORAGE = 7;
    private static final String EXISTING_DOMAIN_STORAGE_NAME = "G95OWd-Wvck-vftu-pMq9-9SAC-NF3E-ulDPsQ";

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;

    @Override
    protected StorageServerConnections generateNewEntity() {
        StorageServerConnections newServerConnection = new StorageServerConnections();
        newServerConnection.setId("0cc146e8-e5ed-482c-8814-270bc48c2980");
        newServerConnection.setConnection(EXISTING_DOMAIN_STORAGE_NAME);
        newServerConnection.setNfsVersion(NfsVersion.V4);
        newServerConnection.setNfsRetrans((short) 5);
        return newServerConnection;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setIqn("1.2.3.4");
        existingEntity.setConnection("/host2/lib/data");
        existingEntity.setNfsRetrans((short) 3);
        existingEntity.setNfsTimeo((short) 100);
    }

    @Override
    protected String getExistingEntityId() {
        return "0cc146e8-e5ed-482c-8814-270bc48c297f";
    }

    @Override
    protected String generateNonExistingId() {
        return "farkle";
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 11;
    }

    @Test
    public void testGetForIqnWithInvalidIqn() {
        StorageServerConnections result = dao.getForIqn("farkle");

        assertNull(result);
    }

    @Test
    public void testGetForIqn() {
        StorageServerConnections result = dao.getForIqn(existingEntity.getIqn());

        assertNotNull(result);
        assertEquals(existingEntity, result);
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
        List<StorageServerConnections> result = dao.getAllConnectableStorageSeverConnection(FixturesTool.DATA_CENTER);

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
                dao.getConnectableStorageConnectionsByStorageType(FixturesTool.DATA_CENTER, null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    @Test
    public void getStorageConnectionsByStorageTypeNoRecordsOfType() {
        List<StorageServerConnections> result =
                dao.getStorageConnectionsByStorageTypeAndStatus(FixturesTool.DATA_CENTER, StorageType.FCP, EnumSet.allOf(StorageDomainStatus.class));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getStorageConnectionsByStorageTypeNoRecordsOfStatus() {
        List<StorageServerConnections> result =
                dao.getStorageConnectionsByStorageTypeAndStatus(FixturesTool.DATA_CENTER, StorageType.NFS, EnumSet.of(StorageDomainStatus.Locked));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getStorageConnectionsByStorageTypeWithRecordsMultipleStatuses() {
        getStorageConnectionsByStorageTypeWithRecords(EnumSet.of(StorageDomainStatus.Maintenance, StorageDomainStatus.Unknown),
                Arrays.asList(FixturesTool.STORAGE_DOMAIN_NFS2_3, FixturesTool.STORAGE_DOMAIN_NFS2_1));
    }

    @Test
    public void getStorageConnectionsByStorageTypeWithRecordsOneStatus() {
        getStorageConnectionsByStorageTypeWithRecords(EnumSet.of(StorageDomainStatus.Maintenance),
                Collections.singletonList(FixturesTool.STORAGE_DOMAIN_NFS2_3));
    }


    public void getStorageConnectionsByStorageTypeWithRecords(EnumSet<StorageDomainStatus> statuses,
            Collection<Guid> expectedDomains) {
        List<StoragePoolIsoMap> poolIsoMap =
                storagePoolIsoMapDao.getAllForStoragePool(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        List<Guid> storageDomainIds = poolIsoMap.stream()
                .filter(isoMap -> statuses.contains(isoMap.getStatus()))
                .map(StoragePoolIsoMap::getStorageId)
                .collect(Collectors.toList());

        assertTrue(CollectionUtils.isEqualCollection(expectedDomains, storageDomainIds),
                "the list of the pool domains expected to be in the given statuses doesn't match the queried data");

        List<StorageServerConnections> result =
                dao.getStorageConnectionsByStorageTypeAndStatus(FixturesTool.STORAGE_POOL_MIXED_TYPES,
                        StorageType.NFS,
                        statuses);

        assertFalse(result.isEmpty(), "there should be connections for the tested domains to verify the correctness");

        for (StorageServerConnections storageServerConnection : result) {
            assertEquals(StorageType.NFS, storageServerConnection.getStorageType(),
                    "connections were loaded with incorrect storage type");
        }

        List<StorageServerConnections> domainConnections = new LinkedList<>();
        for (Guid domainId : storageDomainIds) {
            domainConnections.addAll(dao.getAllForDomain(domainId));
        }

        assertTrue(CollectionUtils.isEqualCollection(domainConnections, result),
                "the connections loaded by the given dao function should match the connections loaded separately");
    }

    private Set<String> getLunConnections(List<LUNStorageServerConnectionMap> lunConns) {
        return lunConns.stream().map(LUNStorageServerConnectionMap::getStorageServerConnection).collect(Collectors.toSet());
    }
    /**
     * Retrieves all connections for the given volume group.
     *
     */
    @Test
    public void testgetAllForVolumeGroup() {
        Set<String> lunConns1 = getLunConnections(storageServerConnectionLunMapDao.getAll(FixturesTool.LUN_ID1));
        Set<String> lunConns2 = getLunConnections(storageServerConnectionLunMapDao.getAll(FixturesTool.LUN_ID2));
        assertTrue(CollectionUtils.containsAny(lunConns1, lunConns2),
                "Both LUNs should have at least one mutual connection");

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
        StorageServerConnections conn = dao.get(existingEntity.getId());
        conn.setId("copy");
        dao.save(conn);
        assertGetAllForConnectionResult(Arrays.asList(existingEntity, conn), existingEntity);
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
     * Ensures NFS options work as expected.
     */
    @Test
    public void testNfsOptions() {
        StorageServerConnections result = dao.get(FixturesTool.EXISTING_STORAGE_CONNECTION_NFS_AUTO_ID.toString());

        assertEquals(NfsVersion.AUTO, result.getNfsVersion());
        assertEquals(Short.valueOf((short) 7), result.getNfsRetrans());
        assertEquals(Short.valueOf((short) 42), result.getNfsTimeo());
    }

    @Test
    public void testGetAllConnectionsOfNfsDomain() {
      List<StorageServerConnections> connections = dao.getAllForDomain(Guid.createGuidFromString("d9ede37f-e6c3-4bf9-a984-19174070aa31"));
      assertEquals(1, connections.size());
      assertEquals("0cc146e8-e5ed-482c-8814-270bc48c2981", connections.get(0).getId());
    }

    @Test
    public void testGetAllConnectionsOfIscsiDomain() {
      List<StorageServerConnections> connections = dao.getAllForDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
      assertEquals(2, connections.size());
      assertTrue(connections.get(0).getId().equals("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk") || connections.get(0).getId().equals("0cc146e8-e5ed-482c-8814-270bc48c297e"));
    }

    @Test
    public void testGetConnectionsBySpecificIds() {
        List<StorageServerConnections> connections = dao.getByIds(Collections.singletonList("0cc146e8-e5ed-482c-8814-270bc48c297f"));
        assertEquals(1, connections.size());
    }
}
