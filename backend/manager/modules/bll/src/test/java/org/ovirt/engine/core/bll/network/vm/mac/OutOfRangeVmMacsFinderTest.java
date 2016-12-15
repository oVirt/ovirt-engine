package org.ovirt.engine.core.bll.network.vm.mac;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class OutOfRangeVmMacsFinderTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();

    @Mock
    private ExternalVmMacsFinder mockExternalVmMacsFinder;

    @Captor
    private ArgumentCaptor<VM> vmCaptor;

    private VM vm;

    private OutOfRangeVmMacsFinder underTest;

    @Before
    public void setUp() {
        underTest = new OutOfRangeVmMacsFinder(mockExternalVmMacsFinder, CLUSTER_ID);
        vm = new VM();
    }

    @Test
    public void testFindProblematicMacs() {
        final Set<String> externalMacs = new HashSet<>();
        when(mockExternalVmMacsFinder.findExternalMacAddresses(vm)).thenReturn(externalMacs);

        final Collection<String> actual = underTest.findProblematicMacs(vm);

        assertThat(actual, sameInstance(externalMacs));
        verify(mockExternalVmMacsFinder).findExternalMacAddresses(vmCaptor.capture());
        assertThat(vmCaptor.getValue().getClusterId(), is(CLUSTER_ID));
    }
}
