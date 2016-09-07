package org.ovirt.engine.core.vdsbroker.vdsbroker.predicates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class DisplayInterfaceEqualityPredicateTest {
    private static final String TEST_INTERFACE_NAME = "interface name";
    private static final String TEST_INTERFACE_ADDRESS = "interface address";

    @Mock
    private VdsNetworkInterface mockIface;
    @Mock
    private VdsNetworkInterface mockOtherIface;

    private DisplayInterfaceEqualityPredicate underTest;

    @Before
    public void setUp() throws Exception {
        when(mockIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        when(mockIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);
        underTest = new DisplayInterfaceEqualityPredicate(mockIface);
    }

    @Test
    public void testEvalPositive() throws Exception {
        when(mockOtherIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        when(mockOtherIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);

        assertTrue(underTest.test(mockOtherIface));
    }

    @Test
    public void testEvalDifferentName() throws Exception {
        when(mockOtherIface.getName()).thenReturn("not" + TEST_INTERFACE_NAME);
        when(mockOtherIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);

        assertFalse(underTest.test(mockOtherIface));
    }

    @Test
    public void testEvalDifferentAddress() throws Exception {
        when(mockOtherIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        when(mockOtherIface.getIpv4Address()).thenReturn("not" + TEST_INTERFACE_ADDRESS);

        assertFalse(underTest.test(mockOtherIface));
    }

}
