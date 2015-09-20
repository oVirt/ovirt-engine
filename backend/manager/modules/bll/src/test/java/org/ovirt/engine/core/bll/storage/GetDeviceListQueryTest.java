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
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;

public class GetDeviceListQueryTest extends AbstractQueryTest<GetDeviceListQueryParameters, GetDeviceListQuery<GetDeviceListQueryParameters>> {
    private DbFacade dbFacadeMock;
    private LunDao lunDaoMock;
    private VdsDao vdsDaoMock;
    private StorageDomainDao storageDomainDaoMock;
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

    /**
     * Test query execution when LUNs filtering is enabled
     */
    @Test
    public void testExecuteQueryWithFilteringLUNsEnabled() {
        mcr.mockConfigValue(ConfigValues.FilteringLUNsEnabled, Version.v3_1, true);

        // Create expected result
        lunsExpected = Collections.emptyList();

        internalExecuteQuery();
    }

    /**
     * Test query execution when LUNs filtering is disabled
     */
    @Test
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
        when(lunDaoMock.getAll()).thenReturn(lunsFromDb);

        // Execute command
        getQuery().executeQueryCommand();

        // Assert the query's results
        List<LUNs> lunsActual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(lunsExpected, lunsActual);
    }

    /**
     * Mock the DbFacade/VDSBroker and the Daos
     */
    private void prepareMocks() {
        dbFacadeMock = getDbFacadeMockInstance();
        vdsBrokerFrontendMock = mock(VDSBrokerFrontend.class);
        doReturn(vdsBrokerFrontendMock).when(getQuery()).getVdsBroker();

        lunDaoMock = mock(LunDao.class);
        when(dbFacadeMock.getLunDao()).thenReturn(lunDaoMock);
        vdsDaoMock = mock(VdsDao.class);
        when(dbFacadeMock.getVdsDao()).thenReturn(vdsDaoMock);
        storageDomainDaoMock = mock(StorageDomainDao.class);
        when(dbFacadeMock.getStorageDomainDao()).thenReturn(storageDomainDaoMock);
    }

    /**
     * Prepare query parameters
     */
    private void prepareParameters() {

        vdsId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(vdsId);

        storageType = StorageType.UNKNOWN;
        when(getQueryParameters().getStorageType()).thenReturn(storageType);

        vds = new VDS();
        vds.setVdsGroupCompatibilityVersion(Version.v3_1);
        when(vdsDaoMock.get(getQueryParameters().getId())).thenReturn(vds);

        List<StorageDomain> domainsList = Collections.emptyList();
        when(storageDomainDaoMock.getAll()).thenReturn(domainsList);
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
