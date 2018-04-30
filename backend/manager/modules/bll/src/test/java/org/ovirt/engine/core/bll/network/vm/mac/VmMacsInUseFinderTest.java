package org.ovirt.engine.core.bll.network.vm.mac;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmMacsInUseFinderTest {

    private static final String MAC1 = "mac1";
    private static final String MAC2 = "mac2";

    @Mock
    private ReadMacPool mockReadMacPool;

    @Mock
    private VM mockVm;

    private VmMacsInUseFinder underTest;

    @BeforeEach
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
