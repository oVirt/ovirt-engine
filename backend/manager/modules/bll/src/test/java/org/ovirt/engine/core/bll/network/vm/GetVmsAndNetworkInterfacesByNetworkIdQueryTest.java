package org.ovirt.engine.core.bll.network.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmsAndNetworkInterfacesByNetworkIdParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

/**
 * A test for the {@link GetVmsAndNetworkInterfacesByNetworkIdQuery} class. It tests the flow (i.e., that the query
 * delegates properly to the Dao}). The internal workings of the Dao are not tested.
 */
public class GetVmsAndNetworkInterfacesByNetworkIdQueryTest
        extends AbstractQueryTest<GetVmsAndNetworkInterfacesByNetworkIdParameters,
        GetVmsAndNetworkInterfacesByNetworkIdQuery<GetVmsAndNetworkInterfacesByNetworkIdParameters>> {

    private Guid networkId = Guid.newGuid();
    private Guid vmId = Guid.newGuid();
    private VM vm = new VM();
    private VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();

    @Mock
    private VmDao vmDaoMock;

    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDaoMock;

    @Mock
    private VmHandler vmHandler;

    @Test
    public void testExecuteQueryCommand() {
        // Set up the Daos
        setupVmDao(Collections.singletonList(vm));
        setupVmNetworkInterfaceDao(Collections.singletonList(vmNetworkInterface));

        expectAndTestForVmAndInterface();
    }

    private void expectAndTestForVmAndInterface() {
        // Set up the query parameters
        when(params.getId()).thenReturn(networkId);

        vm.setId(vmId);
        vmNetworkInterface.setVmId(vmId);

        doNothing().when(vmHandler).updateConfiguredCpuVerb(vm);

        PairQueryable<VmNetworkInterface, VM> vmInterfaceVmPair =
                new PairQueryable<>(vmNetworkInterface, vm);
        List<PairQueryable<VmNetworkInterface, VM>> expected = Collections.singletonList(vmInterfaceVmPair);

        // Run the query
        getQuery().executeQueryCommand();

        // Assert the result
        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result returned");
    }

    @Test
    public void onlyVmsThatAreRunning() {
        expectAndTestForVmsThatAreInAState(true, VMStatus.Up, VMStatus.Down);
    }

    @Test
    public void onlyVmsThatAreNotRunning() {
        expectAndTestForVmsThatAreInAState(false, VMStatus.Down, VMStatus.Up);
    }

    private void expectAndTestForVmsThatAreInAState(
            boolean running, VMStatus expectedStatus, VMStatus unexpectedStatus) {
        when(params.getRunningVms()).thenReturn(running);

        vm.setStatus(expectedStatus);
        VM downVm = new VM();
        downVm.setId(Guid.newGuid());
        downVm.setStatus(unexpectedStatus);

        VmNetworkInterface downVmInterface = new VmNetworkInterface();
        downVmInterface.setVmId(downVm.getId());

        setupVmDao(Arrays.asList(vm, downVm));
        setupVmNetworkInterfaceDao(Arrays.asList(vmNetworkInterface, downVmInterface));

        expectAndTestForVmAndInterface();
    }

    private void setupVmDao(List<VM> expectedVms) {
        when(vmDaoMock.getAllForNetwork(networkId)).thenReturn(expectedVms);
    }

    private void setupVmNetworkInterfaceDao(List<VmNetworkInterface> expectedVmNetworkInterfaces) {
        when(vmNetworkInterfaceDaoMock.getAllForNetwork(networkId)).thenReturn(expectedVmNetworkInterfaces);
    }
}
