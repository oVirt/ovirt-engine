package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * A test case for {@link GetVdsByVdsIdQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetVdsByVdsIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetVdsByVdsIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQueryCommnad() {
        // Set up the expected data
        Guid vdsID = Guid.newGuid();
        VDS expected = new VDS();
        expected.setId(vdsID);

        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(vdsID);

        // Mock the Daos
        VdsDao vdsDaoMock = mock(VdsDao.class);
        when(vdsDaoMock.get(vdsID)).thenReturn(expected);
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDaoMock);

        CpuFlagsManagerHandler cpuFlagsManagerHandlerMock = mock(CpuFlagsManagerHandler.class);
        GetVdsByVdsIdQuery<IdQueryParameters> query = getQuery();
        when(query.getCpuFlagsManagerHandler()).thenReturn(cpuFlagsManagerHandlerMock);

        query.executeQueryCommand();

        VDS actual = query.getQueryReturnValue().getReturnValue();
        assertEquals("wrong VDS", expected, actual);
    }
}
