package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A test case for {@link GetVmByVmNameForDataCenterQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetVmByVmNameForDataCenterQueryTest extends AbstractUserQueryTest<GetVmByVmNameForDataCenterParameters, GetVmByVmNameForDataCenterQuery<GetVmByVmNameForDataCenterParameters>> {
    @Mock
    private VmDao vmDaoMock;

    @Test
    public void testExecuteQuery() {
        Guid dataCenterId = Guid.newGuid();
        String VM_NAME = "VM";
        VM expectedResult = new VM();
        expectedResult.setId(dataCenterId);

        GetVmByVmNameForDataCenterParameters paramsMock = getQueryParameters();
        when(paramsMock.getName()).thenReturn(VM_NAME);
        when(paramsMock.getDataCenterId()).thenReturn(dataCenterId);

        when(vmDaoMock.getByNameForDataCenter(dataCenterId, VM_NAME, getUser().getId(), paramsMock.isFiltered())).thenReturn(expectedResult);

        doNothing().when(getQuery()).updateVMDetails(expectedResult);
        getQuery().executeQueryCommand();

        VM result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expectedResult, result, "Wrong VM returned");
    }
}
