package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmPoolDao;

/**
 * A test case for {@link GetVmPoolByIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetVmPoolByIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVmPoolByIdQuery<IdQueryParameters>> {
    @Mock
    private VmPoolDao vmPoolDaoMock;

    @Test
    public void testExecuteQuery() {
        Guid vmPoolID = Guid.newGuid();
        VmPool expectedResult = new VmPool();
        expectedResult.setVmPoolId(vmPoolID);

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(vmPoolID);

        when(vmPoolDaoMock.get(vmPoolID, getUser().getId(), paramsMock.isFiltered())).thenReturn(expectedResult);

        getQuery().executeQueryCommand();

        VmPool result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expectedResult, result, "Wrong VM pool returned");
    }
}
