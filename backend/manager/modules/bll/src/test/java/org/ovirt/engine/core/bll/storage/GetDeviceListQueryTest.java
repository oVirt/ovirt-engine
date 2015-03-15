package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VdsDAO;

public class GetDeviceListQueryTest extends AbstractQueryTest<GetDeviceListQueryParameters, GetDeviceListQuery<GetDeviceListQueryParameters>> {
    private DbFacade dbFacadeMock;
    private LunDAO lunDAOMock;
    private VdsDAO vdsDAOMock;
    private StorageDomainDAO storageDomainDAOMock;
    private VDSBrokerFrontend vdsBrokerFrontendMock;

    private Guid vdsId;
    private StorageType storageType;
    private VDS vds;

    // LUNs list retrieved from DB
    private List<LUNs> lunsFromDb;

    // LUNs list retrieved from VDSM
    private List<LUNs> lunsInput;

    // LUNs list returned by query
    private List<LUNs> lunsExpected;

    @Test
    /**
     * Test query execution when LUNs filtering is enabled
     */
    public void testExecuteQueryWithFilteringLUNsEnabled() {
        mcr.mockConfigValue(ConfigValues.FilteringLUNsEnabled, Version.v3_1, true);

        // Create expected result
        lunsExpected = Collections.emptyList();

        internalExecuteQuery();
    }

    @Test
    /**
     * Test query execution when LUNs filtering is disabled
     */
    public void testExecuteQueryWithFilteringLUNsDisabled() {
        mcr.mockConfigValue(ConfigValues.FilteringLUNsEnabled, Version.v3_1, false);

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
        when(vdsBrokerFrontendMock.RunVdsCommand(eq(VDSCommandType.GetDeviceList),
                any(GetDeviceListVDSCommandParameters.class))).thenReturn(returnValue);

        // Return 'lunsFromDb'
        when(lunDAOMock.getAll()).thenReturn(lunsFromDb);

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<LUNs> lunsActual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(lunsExpected, lunsActual);
    }

    /**
     * Mock the DbFacade/VDSBroker and the DAOs
     */
    private void prepareMocks() {
        dbFacadeMock = getDbFacadeMockInstance();
        vdsBrokerFrontendMock = mock(VDSBrokerFrontend.class);
        doReturn(vdsBrokerFrontendMock).when(getQuery()).getVdsBroker();

        lunDAOMock = mock(LunDAO.class);
        when(dbFacadeMock.getLunDao()).thenReturn(lunDAOMock);
        vdsDAOMock = mock(VdsDAO.class);
        when(dbFacadeMock.getVdsDao()).thenReturn(vdsDAOMock);
        storageDomainDAOMock = mock(StorageDomainDAO.class);
        when(dbFacadeMock.getStorageDomainDao()).thenReturn(storageDomainDAOMock);
    }

    /**
     * Prepare query parameters
     */
    private void prepareParameters() {

        vdsId = Guid.newGuid();
        when(getQueryParameters().getVdsId()).thenReturn(vdsId);

        storageType = StorageType.UNKNOWN;
        when(getQueryParameters().getStorageType()).thenReturn(storageType);

        vds = new VDS();
        vds.setVdsGroupCompatibilityVersion(Version.v3_1);
        when(vdsDAOMock.get(getQueryParameters().getVdsId())).thenReturn(vds);

        List<StorageDomain> domainsList = Collections.emptyList();
        when(storageDomainDAOMock.getAll()).thenReturn(domainsList);
    }

    /**
     * Create the input list of LUNs
     */
    private void createInputLUNs() {
        LUNs lunWithVG = new LUNs();
        lunWithVG.setLUN_id(Guid.newGuid().toString());
        lunWithVG.setvolume_group_id(Guid.newGuid().toString());

        LUNs lunExistsInDB = new LUNs();
        lunExistsInDB.setLUN_id(Guid.newGuid().toString());

        lunsInput = new ArrayList<LUNs>();
        lunsInput.add(lunWithVG);
        lunsInput.add(lunExistsInDB);

        lunsFromDb = new ArrayList<LUNs>();
        lunsFromDb.add(lunExistsInDB);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        prepareMocks();
        prepareParameters();
        createInputLUNs();
    }
}
