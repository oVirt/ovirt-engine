package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDeviceDAO;

public class IsBalloonEnabledQueryTest extends AbstractQueryTest<IdQueryParameters, IsBalloonEnabledQuery<IdQueryParameters>> {

    /** The {@link VmDeviceDAO} mocked for the test */
    private VmDeviceDAO vmDeviceDAOMock;

    /** The ID of the VM the disks belong to */
    private Guid vmID;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmID = Guid.NewGuid();
        setUpDAOMocks();
    }

    private void setUpDAOMocks() {
        // Mock the DAOs
        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        // VM Device DAO
        vmDeviceDAOMock = mock(VmDeviceDAO.class);
        when(dbFacadeMock.getVmDeviceDao()).thenReturn(vmDeviceDAOMock);
    }

    @Test
    public void testExecuteQueryCommand() {
        params = getQueryParameters();
        when(params.getId()).thenReturn(vmID);

        IsBalloonEnabledQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        assertTrue(!((Boolean) query.getQueryReturnValue().getReturnValue()).booleanValue());
    }
}
