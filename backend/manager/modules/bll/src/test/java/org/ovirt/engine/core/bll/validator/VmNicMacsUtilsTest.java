package org.ovirt.engine.core.bll.validator;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class VmNicMacsUtilsTest {
    @Mock
    private VmNetworkInterface vmNetworkInterfaceMock;

    private VmNicMacsUtils underTest = new VmNicMacsUtils();

    @Test
    public void testReplaceInvalidEmptyStringMacAddressesWithNullDoNotTouchSetMacAddresses() {
        when(vmNetworkInterfaceMock.getMacAddress()).thenReturn("00:0a:95:9d:68:16");

        underTest.replaceInvalidEmptyStringMacAddressesWithNull(singletonList(vmNetworkInterfaceMock));
        verify(vmNetworkInterfaceMock, never()).setMacAddress(Mockito.any());
    }

    @Test
    public void testReplaceInvalidEmptyStringMacAddressesWithNullDoReplaceEmptyString() {
        when(vmNetworkInterfaceMock.getMacAddress()).thenReturn("");

        underTest.replaceInvalidEmptyStringMacAddressesWithNull(singletonList(vmNetworkInterfaceMock));
        verify(vmNetworkInterfaceMock).setMacAddress(Mockito.eq(null));
    }
}
