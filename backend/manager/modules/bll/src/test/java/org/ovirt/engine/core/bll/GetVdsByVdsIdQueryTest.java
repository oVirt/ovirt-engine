package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * A test case for {@link GetVdsByVdsIdQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetVdsByVdsIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetVdsByVdsIdQuery<IdQueryParameters>> {
    @Mock
    private CpuFlagsManagerHandler cpuFlagsManagerHandlerMock;

    @Mock
    private VdsDao vdsDaoMock;

    @Test
    public void testExecuteQueryCommnad() {
        // Set up the expected data
        Guid vdsID = Guid.newGuid();
        VDS expected = new VDS();
        expected.setId(vdsID);

        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(vdsID);

        // Mock the Daos
        when(vdsDaoMock.get(vdsID)).thenReturn(expected);

        GetVdsByVdsIdQuery<IdQueryParameters> query = getQuery();

        query.executeQueryCommand();

        VDS actual = query.getQueryReturnValue().getReturnValue();
        assertEquals(expected, actual, "wrong VDS");
    }
}
