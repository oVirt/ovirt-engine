package org.ovirt.engine.core.bll.network.predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.Network;

@ExtendWith(MockitoExtension.class)
public class ExternalNetworkPredicateTest {

    @Mock
    private Network mockNetwork;

    private Predicate<Network> underTest = new ExternalNetworkPredicate();

    @Test
    public void testEvalPositive() {
        when(mockNetwork.isExternal()).thenReturn(true);

        assertTrue(underTest.test(mockNetwork));
    }

    @Test
    public void testEvalNegative() {
        when(mockNetwork.isExternal()).thenReturn(false);

        assertFalse(underTest.test(mockNetwork));
    }
}
