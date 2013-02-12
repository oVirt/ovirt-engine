package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;

/**
 * A test case for {@link GetVdsByVdsIdQuery}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetVdsByVdsIdQueryTest extends AbstractQueryTest<GetVdsByVdsIdParameters, GetVdsByVdsIdQuery<GetVdsByVdsIdParameters>> {

    @Test
    public void testExecuteQueryCommnad() {
        // Set up the expected data
        Guid vdsID = Guid.NewGuid();
        VDS expected = new VDS();
        expected.setId(vdsID);

        // Mock the query's parameters
        when(getQueryParameters().getVdsId()).thenReturn(vdsID);

        // Mock the DAOs
        VdsDAO vdsDAOMock = mock(VdsDAO.class);
        when(vdsDAOMock.get(vdsID)).thenReturn(expected);
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDAOMock);

        getQuery().executeQueryCommand();

        VDS actual = (VDS) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("wrong VDS", expected, actual);
    }
}
