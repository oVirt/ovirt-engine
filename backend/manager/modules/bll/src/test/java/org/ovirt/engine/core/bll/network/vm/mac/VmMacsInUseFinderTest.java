package org.ovirt.engine.core.bll.network.vm.mac;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class VmMacsInUseFinderTest {

    private static final String MAC1 = "mac1";
    private static final String MAC2 = "mac2";

    @Mock
    private ReadMacPool mockReadMacPool;

    @Mock
    private VM mockVm;

    private VmMacsInUseFinder underTest;

    @Before
    public void setUp() {
        underTest = new VmMacsInUseFinder(mockReadMacPool);
        when(mockVm.getInterfaces()).thenReturn(createVnics(MAC1, MAC2));
        when(mockReadMacPool.isMacInUse(MAC1)).thenReturn(false);
        when(mockReadMacPool.isMacInUse(MAC2)).thenReturn(true);
    }

    @Test
    public void testFindProblematicMacs() {
        when(mockReadMacPool.isDuplicateMacAddressesAllowed()).thenReturn(false);

        final Collection<String> actual = underTest.findProblematicMacs(mockVm);

        assertThat(actual, contains(MAC2));
    }

    @Test
    public void testFindProblematicMacsDuplicateMacAddressesAllowed() {
        when(mockReadMacPool.isDuplicateMacAddressesAllowed()).thenReturn(true);

        final Collection<String> actual = underTest.findProblematicMacs(mockVm);

        assertThat(actual, empty());
    }

    private List<VmNetworkInterface> createVnics(String... macs) {
        return Arrays.stream(macs)
                .map(mac -> {
                    final VmNetworkInterface vnic = new VmNetworkInterface();
                    vnic.setMacAddress(mac);
                    return vnic;
                })
                .collect(Collectors.toList());
    }
}
