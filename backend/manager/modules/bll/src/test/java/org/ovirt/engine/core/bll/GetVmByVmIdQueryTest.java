package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;

/**
 * A test case for {@link GetVmByVmIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the DAO occur.
 */
public class GetVmByVmIdQueryTest extends AbstractUserQueryTest<GetVmByVmIdParameters, GetVmByVmIdQuery<GetVmByVmIdParameters>> {
    @Test
    public void testExecuteQuery() {
        Guid vmID = Guid.NewGuid();
        VM expectedResult = new VM();
        expectedResult.setId(vmID);

        GetVmByVmIdParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(vmID);

        VmDAO vmDAOMock = mock(VmDAO.class);
        when(vmDAOMock.get(vmID, getUser().getUserId(), paramsMock.isFiltered())).thenReturn(expectedResult);

        DiskImage disk = new DiskImage();
        disk.setvm_guid(vmID);
        disk.setactive(true);
        DiskImageDAO diskImageDAO = mock(DiskImageDAO.class);
        when(diskImageDAO.getAllForVm(vmID)).thenReturn(Collections.singletonList(disk));

        VmNetworkInterface netwrokInterface = new VmNetworkInterface();
        netwrokInterface.setVmId(vmID);
        VmNetworkInterfaceDAO vmNetworkInterfaceDAO = mock(VmNetworkInterfaceDAO.class);
        when(vmNetworkInterfaceDAO.getAllForVm(vmID)).thenReturn(Collections.singletonList(netwrokInterface));

        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        when(dbFacadeMock.getVmDAO()).thenReturn(vmDAOMock);
        when(dbFacadeMock.getDiskImageDAO()).thenReturn(diskImageDAO);
        when(dbFacadeMock.getVmNetworkInterfaceDAO()).thenReturn(vmNetworkInterfaceDAO);

        getQuery().executeQueryCommand();

        VM result = (VM) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong VM returned", expectedResult, result);
        assertEquals("Wrong number of disks on the VM", 1, result.getDiskList().size());
        assertEquals("Wrong disk on the VM", disk, result.getDiskList().get(0));
        assertEquals("Wrong number of network interfaces on the VM", 1, result.getInterfaces().size());
        assertEquals("Wrong disk on the VM", netwrokInterface, result.getInterfaces().get(0));
    }
}
