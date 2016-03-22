package org.ovirt.engine.core.bll.storage.disk.lun;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;

public class GetDeviceListQueryTest extends AbstractQueryTest<GetDeviceListQueryParameters, GetDeviceListQuery<GetDeviceListQueryParameters>> {
    private LunDao lunDaoMock;
    private VDSBrokerFrontend vdsBrokerFrontendMock;

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
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.GetDeviceList),
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
        vdsBrokerFrontendMock = mock(VDSBrokerFrontend.class);
        doReturn(vdsBrokerFrontendMock).when(getQuery()).getVdsBroker();

        lunDaoMock = mock(LunDao.class);
        when(getDbFacadeMockInstance().getLunDao()).thenReturn(lunDaoMock);
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
    @Before
    public void setUp() throws Exception {
        super.setUp();

        prepareMocks();
        createInputLUNs();
    }
}
