package org.ovirt.engine.core.bll.network.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;

@RunWith(MockitoJUnitRunner.class)
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
