package org.ovirt.engine.core.vdsbroker.vdsbroker.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@ExtendWith(MockitoExtension.class)
public class IsNetworkOnInterfacePredicateTest {

    private static final String TEST_NETWORK_NAME = "network name";
    @Mock
    private VdsNetworkInterface mockVdsNetworkInterface;

    @Test
    public void testEvalPositive() {
        when(mockVdsNetworkInterface.getNetworkName()).thenReturn(TEST_NETWORK_NAME);

        final IsNetworkOnInterfacePredicate underTest = new IsNetworkOnInterfacePredicate(TEST_NETWORK_NAME);

        assertTrue(underTest.test(mockVdsNetworkInterface));
    }

    @Test
    public void testEvalNegaitive() {
        when(mockVdsNetworkInterface.getNetworkName()).thenReturn("not" + TEST_NETWORK_NAME);

        final IsNetworkOnInterfacePredicate underTest = new IsNetworkOnInterfacePredicate(TEST_NETWORK_NAME);

        assertFalse(underTest.test(mockVdsNetworkInterface));
    }
}
