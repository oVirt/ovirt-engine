package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GetManagementInterfaceAddressByVmIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetManagementInterfaceAddressByVmIdQuery<IdQueryParameters>> {
    @Mock
    private VmDao vmDaoMock;

    @Mock
    private InterfaceDao interfaceDaoMock;

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

        when(vmDaoMock.get(vmID)).thenReturn(vm);
        when(interfaceDaoMock.getManagedInterfaceForVds(vdsID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(managementInterface);

        getQuery().executeQueryCommand();

        String result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("my_address", result, "Wrong address returned");
    }
}
