package org.ovirt.engine.core.bll.network.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExternalVmMacsFinderTest {

    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String MAC_ADDRESS_1 = "mac address 1";
    private static final String MAC_ADDRESS_2 = "mac address 2";

    @Mock
    private MacPoolPerCluster mockMacPoolPerCluster;

    @Mock
    private ReadMacPool mockReadMacPool;

    @InjectMocks
    private ExternalVmMacsFinder underTest;

    private VM vm;
    private VmNetworkInterface vNic1 = new VmNetworkInterface();
    private VmNetworkInterface vNic2 = new VmNetworkInterface();

    @BeforeEach
    public void setUp() {
        vm = createVm();

        when(mockMacPoolPerCluster.getMacPoolForCluster(CLUSTER_ID)).thenReturn(mockReadMacPool);
    }

    private VM createVm() {
        vm = new VM();
        vm.setClusterId(CLUSTER_ID);
        vNic1.setMacAddress(MAC_ADDRESS_1);
        vNic2.setMacAddress(MAC_ADDRESS_2);
        return vm;
    }

    @Test
    public void testFindExternalMacAddresses() {
        when(mockReadMacPool.isMacInRange(MAC_ADDRESS_1)).thenReturn(Boolean.TRUE);
        when(mockReadMacPool.isMacInRange(MAC_ADDRESS_2)).thenReturn(Boolean.FALSE);
        vm.setInterfaces(Arrays.asList(vNic1, vNic2));

        final Set<String> actual = underTest.findExternalMacAddresses(vm);

        assertThat(actual, contains(MAC_ADDRESS_2));
    }

    @Test
    public void testFindExternalMacAddressesVnicsNull() {
        final Set<String> actual = underTest.findExternalMacAddresses(vm);

        assertThat(actual, empty());
    }
}
