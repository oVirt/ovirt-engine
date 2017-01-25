package org.ovirt.engine.core.bll.validator;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class VmNicMacsUtilsTest {
    private static final Predicate<VmNetworkInterface> ALWAYS_FALSE = x -> false;
    private static final Predicate<VmNetworkInterface> ALWAYS_TRUE = x -> true;

    @Mock
    private VmNetworkInterface vmNetworkInterfaceMock;

    @Mock
    private MacPool macPoolMock;

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

    @Test
    public void testValidateThereIsEnoughOfFreeMacsNoAllocations() {
        final ValidationResult actual =
                underTest.validateThereIsEnoughOfFreeMacs(singletonList(vmNetworkInterfaceMock),
                        macPoolMock,
                        ALWAYS_FALSE);

        assertThat(actual, isValid());
    }

    @Test
    public void testValidateThereIsEnoughOfFreeMacsPositive() {
        when(macPoolMock.getAvailableMacsCount()).thenReturn(1);

        final ValidationResult actual =
                underTest.validateThereIsEnoughOfFreeMacs(singletonList(vmNetworkInterfaceMock),
                        macPoolMock,
                        ALWAYS_TRUE);

        assertThat(actual, isValid());
    }

    @Test
    public void testValidateThereIsEnoughOfFreeMacsNegative() {
        when(macPoolMock.getAvailableMacsCount()).thenReturn(0);

        final ValidationResult actual =
                underTest.validateThereIsEnoughOfFreeMacs(singletonList(vmNetworkInterfaceMock),
                        macPoolMock,
                        ALWAYS_TRUE);

        assertThat(actual, failsWith(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES));
    }
}
