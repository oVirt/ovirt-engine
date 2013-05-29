package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GetManagementInterfaceAddressByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetManagementInterfaceAddressByVmIdQuery<IdQueryParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid vmID = Guid.NewGuid();
        VM vm = new VM();
        Guid vdsID = Guid.NewGuid();
        vm.setRunOnVds(vdsID.getValue());

        VDS vds = new VDS();
        vds.setId(vdsID.getValue());
        VdsNetworkInterface managementInterface = new VdsNetworkInterface();
        managementInterface.setAddress("my_address");

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(vmID);

        InterfaceDao interfaceDAOMock = mock(InterfaceDao.class);
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
