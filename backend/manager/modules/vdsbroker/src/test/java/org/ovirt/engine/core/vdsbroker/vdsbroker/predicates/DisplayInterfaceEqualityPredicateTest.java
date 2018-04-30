package org.ovirt.engine.core.vdsbroker.vdsbroker.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@ExtendWith(MockitoExtension.class)
public class DisplayInterfaceEqualityPredicateTest {
    private static final String TEST_INTERFACE_NAME = "interface name";
    private static final String TEST_INTERFACE_ADDRESS = "interface address";

    @Mock
    private VdsNetworkInterface mockIface;
    @Mock
    private VdsNetworkInterface mockOtherIface;

    private DisplayInterfaceEqualityPredicate underTest;

    @BeforeEach
    public void setUp() {
        when(mockIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        when(mockIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);
        underTest = new DisplayInterfaceEqualityPredicate(mockIface);
    }

    @Test
    public void testEvalPositive() {
        when(mockOtherIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        when(mockOtherIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);

        assertTrue(underTest.test(mockOtherIface));
    }

    @Test
    public void testEvalDifferentName() {
        when(mockOtherIface.getName()).thenReturn("not" + TEST_INTERFACE_NAME);
        when(mockOtherIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);

        assertFalse(underTest.test(mockOtherIface));
    }

    @Test
    public void testEvalDifferentAddress() {
        when(mockOtherIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        when(mockOtherIface.getIpv4Address()).thenReturn("not" + TEST_INTERFACE_ADDRESS);

        assertFalse(underTest.test(mockOtherIface));
    }

}
