package org.ovirt.engine.core.vdsbroker.vdsbroker.predicates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class IsNetworkOnInterfacePredicateTest {

    private static final String TEST_NETWORK_NAME = "network name";
    @Mock
    private VdsNetworkInterface mockVdsNetworkInterface;

    @Test
    public void testEvalPositive() throws Exception {
        when(mockVdsNetworkInterface.getNetworkName()).thenReturn(TEST_NETWORK_NAME);

        final IsNetworkOnInterfacePredicate underTest = new IsNetworkOnInterfacePredicate(TEST_NETWORK_NAME);

        assertTrue(underTest.test(mockVdsNetworkInterface));
    }

    @Test
    public void testEvalNegaitive() throws Exception {
        when(mockVdsNetworkInterface.getNetworkName()).thenReturn("not" + TEST_NETWORK_NAME);

        final IsNetworkOnInterfacePredicate underTest = new IsNetworkOnInterfacePredicate(TEST_NETWORK_NAME);

        assertFalse(underTest.test(mockVdsNetworkInterface));
    }
}
