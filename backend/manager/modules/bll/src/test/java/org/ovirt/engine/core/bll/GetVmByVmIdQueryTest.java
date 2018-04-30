package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A test case for {@link GetVmByVmIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetVmByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVmByVmIdQuery<IdQueryParameters>> {
    @Mock
    private VmDao vmDaoMock;

    @Test
    public void testExecuteQuery() {
        Guid vmID = Guid.newGuid();
        VM expectedResult = new VM();
        expectedResult.setId(vmID);

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(vmID);

        when(vmDaoMock.get(vmID, getUser().getId(), paramsMock.isFiltered())).thenReturn(expectedResult);

        doNothing().when(getQuery()).updateVMDetails(expectedResult);
        getQuery().executeQueryCommand();

        VM result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expectedResult, result, "Wrong VM returned");
    }
}
