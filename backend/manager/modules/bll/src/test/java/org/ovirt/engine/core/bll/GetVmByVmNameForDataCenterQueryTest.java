package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;

/**
 * A test case for {@link GetVmByVmNameForDataCenterQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the DAO occur.
 */
public class GetVmByVmNameForDataCenterQueryTest extends AbstractUserQueryTest<GetVmByVmNameForDataCenterParameters, GetVmByVmNameForDataCenterQuery<GetVmByVmNameForDataCenterParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid dataCenterId = Guid.newGuid();
        String VM_NAME = "VM";
        VM expectedResult = new VM();
        expectedResult.setId(dataCenterId);

        GetVmByVmNameForDataCenterParameters paramsMock = getQueryParameters();
        when(paramsMock.getName()).thenReturn(VM_NAME);
        when(paramsMock.getDataCenterId()).thenReturn(dataCenterId);

        VmDAO vmDAOMock = mock(VmDAO.class);
        when(vmDAOMock.getByNameForDataCenter(dataCenterId, VM_NAME, getUser().getUserId(), paramsMock.isFiltered())).thenReturn(expectedResult);
        when(getQuery().getDbFacade().getVmDao()).thenReturn(vmDAOMock);

        doNothing().when(getQuery()).updateVMDetails(expectedResult);
        getQuery().executeQueryCommand();

        VM result = (VM) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong VM returned", expectedResult, result);
    }
}
