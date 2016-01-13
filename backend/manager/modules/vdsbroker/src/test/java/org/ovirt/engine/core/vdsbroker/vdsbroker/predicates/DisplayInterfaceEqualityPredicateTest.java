package org.ovirt.engine.core.vdsbroker.vdsbroker.predicates;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
        Mockito.when(mockIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        Mockito.when(mockIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);
        underTest = new DisplayInterfaceEqualityPredicate(mockIface);
    }

    @Test
    public void testEvalPositive() throws Exception {
        Mockito.when(mockOtherIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        Mockito.when(mockOtherIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);

        Assert.assertTrue(underTest.test(mockOtherIface));
    }

    @Test
    public void testEvalDifferentName() throws Exception {
        Mockito.when(mockOtherIface.getName()).thenReturn("not" + TEST_INTERFACE_NAME);
        Mockito.when(mockOtherIface.getIpv4Address()).thenReturn(TEST_INTERFACE_ADDRESS);

        Assert.assertFalse(underTest.test(mockOtherIface));
    }

    @Test
    public void testEvalDifferentAddress() throws Exception {
        Mockito.when(mockOtherIface.getName()).thenReturn(TEST_INTERFACE_NAME);
        Mockito.when(mockOtherIface.getIpv4Address()).thenReturn("not" + TEST_INTERFACE_ADDRESS);

        Assert.assertFalse(underTest.test(mockOtherIface));
    }

}
