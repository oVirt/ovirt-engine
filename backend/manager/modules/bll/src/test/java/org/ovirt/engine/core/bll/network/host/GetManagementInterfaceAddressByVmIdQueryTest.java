package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.dao.VmDAO;

public class GetManagementInterfaceAddressByVmIdQueryTest extends AbstractUserQueryTest<GetVmByVmIdParameters, GetManagementInterfaceAddressByVmIdQuery<GetVmByVmIdParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid vmID = Guid.NewGuid();
        VM vm = new VM();
        NGuid vdsID = NGuid.NewGuid();
        vm.setRunOnVds(vdsID.getValue());

        VDS vds = new VDS();
        vds.setId(vdsID.getValue());
        VdsNetworkInterface managementInterface = new VdsNetworkInterface();
        managementInterface.setAddress("my_address");

        GetVmByVmIdParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(vmID);

        InterfaceDAO interfaceDAOMock = mock(InterfaceDAO.class);
        VmDAO vmDAOMock = mock(VmDAO.class);
        when(getDbFacadeMockInstance().getVmDao()).thenReturn(vmDAOMock);
        when(vmDAOMock.get(vmID)).thenReturn(vm);
        when(interfaceDAOMock.getManagedInterfaceForVds(vdsID.getValue(), getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn(managementInterface);
        when(getDbFacadeMockInstance().getInterfaceDao()).thenReturn(interfaceDAOMock);

        getQuery().executeQueryCommand();

        String result = (String) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong address returned", "my_address", result);
    }
}
