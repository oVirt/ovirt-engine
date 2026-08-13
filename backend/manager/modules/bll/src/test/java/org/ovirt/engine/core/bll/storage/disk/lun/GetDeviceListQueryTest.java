package org.ovirt.engine.core.bll.storage.disk.lun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

public class GetDeviceListQueryTest extends AbstractQueryTest<GetDeviceListQueryParameters, GetDeviceListQuery<GetDeviceListQueryParameters>> {
    @Mock
    private LunDao lunDaoMock;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontendMock;

    @Mock
    private StorageServerConnectionDao storageServerConnectionDaoMock;

    @Mock
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDaoMock;

    // LUNs list retrieved from DB
    private List<LUNs> lunsFromDb;

    // LUNs list retrieved from VDSM
    private List<LUNs> lunsInput;

    // LUNs list returned by query
    private List<LUNs> lunsExpected;

    /**
     * Test query execution when LUNs filtering is disabled
     */
    @Test
    public void testExecuteQueryWithFilteringLUNsDisabled() {
        // Create expected result
        lunsExpected = lunsInput;

        internalExecuteQuery();
    }

    /**
     * Test query execution
     */
    private void internalExecuteQuery() {
        // Run 'GetDeviceList' command
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(lunsInput);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.GetDeviceList), any())).thenReturn(returnValue);

        // Return 'lunsFromDb'
        when(lunDaoMock.getAll()).thenReturn(lunsFromDb);

        // Return empty connections list
        when(storageServerConnectionDaoMock.getAll()).thenReturn(new ArrayList<>());

        // No LUN-to-connection mappings
        when(storageServerConnectionLunMapDaoMock.getAll()).thenReturn(new ArrayList<>());

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<LUNs> lunsActual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(lunsExpected, lunsActual);
    }

    /**
     * Test that LUN connections returned by VDSM are enriched with their
     * database IDs so the frontend can determine which connections are persisted.
     */
    @Test
    public void testExecuteQueryEnrichesLunConnectionsWithDbIds() {
        // LUN from VDSM with a connection that has no DB ID
        String lunId = Guid.newGuid().toString();
        LUNs lunFromVdsm = new LUNs();
        lunFromVdsm.setLUNId(lunId);
        StorageServerConnections vdsmConnection = new StorageServerConnections();
        vdsmConnection.setIqn("iqn.test:target");
        vdsmConnection.setConnection("192.168.1.1");
        vdsmConnection.setPort("3260");
        List<StorageServerConnections> lunConnections = new ArrayList<>();
        lunConnections.add(vdsmConnection);
        lunFromVdsm.setLunConnections(lunConnections);

        lunsInput = new ArrayList<>();
        lunsInput.add(lunFromVdsm);

        // Same LUN in DB
        LUNs lunFromDb = new LUNs();
        lunFromDb.setLUNId(lunId);
        lunsFromDb = new ArrayList<>();
        lunsFromDb.add(lunFromDb);

        // Matching connection in DB with an ID
        String connectionId = Guid.newGuid().toString();
        StorageServerConnections dbConnection = new StorageServerConnections();
        dbConnection.setId(connectionId);
        dbConnection.setIqn("iqn.test:target");
        dbConnection.setConnection("192.168.1.1");
        dbConnection.setPort("3260");
        List<StorageServerConnections> dbConnections = new ArrayList<>();
        dbConnections.add(dbConnection);

        // Run query
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(lunsInput);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.GetDeviceList), any())).thenReturn(returnValue);
        when(lunDaoMock.getAll()).thenReturn(lunsFromDb);
        when(storageServerConnectionDaoMock.getAll()).thenReturn(dbConnections);

        // The DB connection is mapped to a LUN, so it is considered persisted
        LUNStorageServerConnectionMap mapEntry = new LUNStorageServerConnectionMap(lunId, connectionId);
        when(storageServerConnectionLunMapDaoMock.getAll()).thenReturn(Collections.singletonList(mapEntry));

        getQuery().executeQueryCommand();

        // Assert the LUN connection was enriched with the DB ID
        List<LUNs> lunsActual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, lunsActual.size());
        assertEquals(1, lunsActual.get(0).getLunConnections().size());
        assertEquals(connectionId, lunsActual.get(0).getLunConnections().get(0).getId());
    }

    /**
     * Test that a connection present in the DB but not mapped to any LUN (a
     * floating connection, e.g. detached and kept for re-attachment) is NOT
     * enriched with a DB id, so the frontend keeps offering disconnect.
     */
    @Test
    public void testExecuteQueryDoesNotEnrichFloatingConnections() {
        // LUN from VDSM with a connection that has no DB ID
        String lunId = Guid.newGuid().toString();
        LUNs lunFromVdsm = new LUNs();
        lunFromVdsm.setLUNId(lunId);
        StorageServerConnections vdsmConnection = new StorageServerConnections();
        vdsmConnection.setIqn("iqn.test:target");
        vdsmConnection.setConnection("192.168.1.1");
        vdsmConnection.setPort("3260");
        List<StorageServerConnections> lunConnections = new ArrayList<>();
        lunConnections.add(vdsmConnection);
        lunFromVdsm.setLunConnections(lunConnections);

        lunsInput = new ArrayList<>();
        lunsInput.add(lunFromVdsm);

        // Same LUN in DB
        LUNs lunFromDb = new LUNs();
        lunFromDb.setLUNId(lunId);
        lunsFromDb = new ArrayList<>();
        lunsFromDb.add(lunFromDb);

        // The connection exists in the DB (with an id) but is NOT mapped to any LUN
        String connectionId = Guid.newGuid().toString();
        StorageServerConnections dbConnection = new StorageServerConnections();
        dbConnection.setId(connectionId);
        dbConnection.setIqn("iqn.test:target");
        dbConnection.setConnection("192.168.1.1");
        dbConnection.setPort("3260");
        List<StorageServerConnections> dbConnections = new ArrayList<>();
        dbConnections.add(dbConnection);

        // Run query
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(lunsInput);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.GetDeviceList), any())).thenReturn(returnValue);
        when(lunDaoMock.getAll()).thenReturn(lunsFromDb);
        when(storageServerConnectionDaoMock.getAll()).thenReturn(dbConnections);
        // No LUN-to-connection mappings -> the DB connection is floating
        when(storageServerConnectionLunMapDaoMock.getAll()).thenReturn(new ArrayList<>());

        getQuery().executeQueryCommand();

        // Assert the LUN connection was NOT enriched with the DB id
        List<LUNs> lunsActual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, lunsActual.size());
        assertEquals(1, lunsActual.get(0).getLunConnections().size());
        assertNull(lunsActual.get(0).getLunConnections().get(0).getId());
    }

    /**
     * Test that two DB connections to the same target (same iqn+address+port)
     * but different portal (target portal group / tpgt) are kept distinct, so a
     * pathlist connection is enriched with the id of the connection matching its
     * own portal -- not an arbitrary collision winner.
     */
    @Test
    public void testExecuteQueryDistinguishesConnectionsByPortal() {
        String iqn = "iqn.test:target";
        String address = "192.168.1.1";
        String port = "3260";
        String lunId = Guid.newGuid().toString();

        // LUN from VDSM logged in via portal/tpgt 2
        LUNs lunFromVdsm = new LUNs();
        lunFromVdsm.setLUNId(lunId);
        StorageServerConnections vdsmConnection = new StorageServerConnections();
        vdsmConnection.setIqn(iqn);
        vdsmConnection.setConnection(address);
        vdsmConnection.setPort(port);
        vdsmConnection.setPortal("2");
        lunFromVdsm.setLunConnections(Collections.singletonList(vdsmConnection));

        lunsInput = new ArrayList<>();
        lunsInput.add(lunFromVdsm);

        lunsFromDb = new ArrayList<>();
        LUNs lunFromDb = new LUNs();
        lunFromDb.setLUNId(lunId);
        lunsFromDb.add(lunFromDb);

        // Two DB connections: same iqn/address/port, different portal (tpgt)
        String connectionIdPortal1 = Guid.newGuid().toString();
        StorageServerConnections dbConnectionPortal1 = new StorageServerConnections();
        dbConnectionPortal1.setId(connectionIdPortal1);
        dbConnectionPortal1.setIqn(iqn);
        dbConnectionPortal1.setConnection(address);
        dbConnectionPortal1.setPort(port);
        dbConnectionPortal1.setPortal("1");

        String connectionIdPortal2 = Guid.newGuid().toString();
        StorageServerConnections dbConnectionPortal2 = new StorageServerConnections();
        dbConnectionPortal2.setId(connectionIdPortal2);
        dbConnectionPortal2.setIqn(iqn);
        dbConnectionPortal2.setConnection(address);
        dbConnectionPortal2.setPort(port);
        dbConnectionPortal2.setPortal("2");

        List<StorageServerConnections> dbConnections = new ArrayList<>();
        dbConnections.add(dbConnectionPortal1);
        dbConnections.add(dbConnectionPortal2);

        // Both are mapped, so the mapped-connection gate does not interfere --
        // this isolates the connectionKey distinction.
        List<LUNStorageServerConnectionMap> maps = new ArrayList<>();
        maps.add(new LUNStorageServerConnectionMap(lunId, connectionIdPortal1));
        maps.add(new LUNStorageServerConnectionMap(lunId, connectionIdPortal2));

        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(lunsInput);
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.GetDeviceList), any())).thenReturn(returnValue);
        when(lunDaoMock.getAll()).thenReturn(lunsFromDb);
        when(storageServerConnectionDaoMock.getAll()).thenReturn(dbConnections);
        when(storageServerConnectionLunMapDaoMock.getAll()).thenReturn(maps);

        getQuery().executeQueryCommand();

        // The pathlist connection (portal 2) must take the portal-2 DB id,
        // not the portal-1 id that a 3-field key would have collided onto.
        List<LUNs> lunsActual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, lunsActual.size());
        assertEquals(connectionIdPortal2, lunsActual.get(0).getLunConnections().get(0).getId());
    }

    /**
     * Create the input list of LUNs
     */
    private void createInputLUNs() {
        LUNs lunWithVG = new LUNs();
        lunWithVG.setLUNId(Guid.newGuid().toString());
        lunWithVG.setVolumeGroupId(Guid.newGuid().toString());

        LUNs lunExistsInDB = new LUNs();
        lunExistsInDB.setLUNId(Guid.newGuid().toString());

        lunsInput = new ArrayList<>();
        lunsInput.add(lunWithVG);
        lunsInput.add(lunExistsInDB);

        lunsFromDb = new ArrayList<>();
        lunsFromDb.add(lunExistsInDB);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        createInputLUNs();
    }
}
