package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmsAndNetworkInterfacesByNetworkIdParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;

/**
 * A test for the {@link GetVmsAndNetworkInterfacesByNetworkIdQuery} class. It tests the flow (i.e., that the query
 * delegates properly to the DAO}). The internal workings of the DAO are not tested.
 */
public class GetVmsAndNetworkInterfacesByNetworkIdQueryTest
        extends AbstractQueryTest<GetVmsAndNetworkInterfacesByNetworkIdParameters,
        GetVmsAndNetworkInterfacesByNetworkIdQuery<GetVmsAndNetworkInterfacesByNetworkIdParameters>> {

    private Guid networkId = Guid.NewGuid();
    private Guid vmId = Guid.NewGuid();
    private VM vm = new VM();
    private VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();

    @Test
    public void testExecuteQueryCommand() {
        // Set up the query parameters
        when(params.getNetworkId()).thenReturn(networkId);

        vm.setId(vmId);
        vmNetworkInterface.setVmId(vmId);

        // Set up the DAOs
        setupVmDao();
        setupVmNetworkInterfaceDao();

        PairQueryable<VmNetworkInterface, VM> vmInterfaceVmPair =
                new PairQueryable<VmNetworkInterface, VM>(vmNetworkInterface, vm);
        List<PairQueryable<VmNetworkInterface, VM>> expected = Collections.singletonList(vmInterfaceVmPair);

        // Run the query
        getQuery().executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    private void setupVmDao() {
        List<VM> expectedVm = Collections.singletonList(vm);
        VmDAO vmDaoMock = mock(VmDAO.class);
        when(vmDaoMock.getAllForNetwork(networkId)).thenReturn(expectedVm);
        when(getDbFacadeMockInstance().getVmDao()).thenReturn(vmDaoMock);
    }

    private void setupVmNetworkInterfaceDao() {
        List<VmNetworkInterface> expectedVmNetworkInterface = Collections.singletonList(vmNetworkInterface);
        VmNetworkInterfaceDAO vmNetworkInterfaceDaoMock = mock(VmNetworkInterfaceDAO.class);
        when(vmNetworkInterfaceDaoMock.getAllForNetwork(networkId)).thenReturn(expectedVmNetworkInterface);
        when(getDbFacadeMockInstance().getVmNetworkInterfaceDao()).thenReturn(vmNetworkInterfaceDaoMock);
    }
}
