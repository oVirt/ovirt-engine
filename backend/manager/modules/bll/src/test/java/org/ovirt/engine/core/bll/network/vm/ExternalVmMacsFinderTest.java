package org.ovirt.engine.core.bll.network.vm;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class ExternalVmMacsFinderTest {

    private static final String SESSION_ID = "session id";
    private static final CommandContext COMMAND_CONTEXT = CommandContext.createContext(SESSION_ID);
    private static final Guid DC_ID = Guid.newGuid();
    private static final String MAC_ADDRESS_1 = "mac address 1";
    private static final String MAC_ADDRESS_2 = "mac address 2";

    @Mock
    private MacPoolPerDc mockMacPoolPerDc;

    @Mock
    private MacPool mockMacPool;

    @InjectMocks
    private ExternalVmMacsFinder underTest;

    private VM vm;
    private VmNetworkInterface vNic1 = new VmNetworkInterface();
    private VmNetworkInterface vNic2 = new VmNetworkInterface();

    @Before
    public void setUp() {
        vm = createVm();

        when(mockMacPoolPerDc.getMacPoolForDataCenter(DC_ID, COMMAND_CONTEXT)).thenReturn(mockMacPool);
    }

    private VM createVm() {
        vm = new VM();
        vm.setStoragePoolId(DC_ID);
        vNic1.setMacAddress(MAC_ADDRESS_1);
        vNic2.setMacAddress(MAC_ADDRESS_2);
        return vm;
    }

    @Test
    public void testFindExternalMacAddresses() {
        when(mockMacPool.isMacInRange(MAC_ADDRESS_1)).thenReturn(Boolean.TRUE);
        when(mockMacPool.isMacInRange(MAC_ADDRESS_2)).thenReturn(Boolean.FALSE);
        vm.setInterfaces(Arrays.asList(vNic1, vNic2));

        final Set<String> actual = underTest.findExternalMacAddresses(vm, COMMAND_CONTEXT);

        assertThat(actual, contains(MAC_ADDRESS_2));
    }

    @Test
    public void testFindExternalMacAddressesVnicsNull() {
        final Set<String> actual = underTest.findExternalMacAddresses(vm, COMMAND_CONTEXT);

        assertThat(actual, empty());
    }
}
