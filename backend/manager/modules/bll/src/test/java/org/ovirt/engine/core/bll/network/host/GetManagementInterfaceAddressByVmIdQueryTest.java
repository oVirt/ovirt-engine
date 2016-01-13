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
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GetManagementInterfaceAddressByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetManagementInterfaceAddressByVmIdQuery<IdQueryParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid vmID = Guid.newGuid();
        VM vm = new VM();
        Guid vdsID = Guid.newGuid();
        vm.setRunOnVds(vdsID);

        VDS vds = new VDS();
        vds.setId(vdsID);
        VdsNetworkInterface managementInterface = new VdsNetworkInterface();
        managementInterface.setIpv4Address("my_address");

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(vmID);

        InterfaceDao interfaceDaoMock = mock(InterfaceDao.class);
        VmDao vmDaoMock = mock(VmDao.class);
        when(getDbFacadeMockInstance().getVmDao()).thenReturn(vmDaoMock);
        when(vmDaoMock.get(vmID)).thenReturn(vm);
        when(interfaceDaoMock.getManagedInterfaceForVds(vdsID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(managementInterface);
        when(getDbFacadeMockInstance().getInterfaceDao()).thenReturn(interfaceDaoMock);

        getQuery().executeQueryCommand();

        String result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong address returned", "my_address", result);
    }
}
