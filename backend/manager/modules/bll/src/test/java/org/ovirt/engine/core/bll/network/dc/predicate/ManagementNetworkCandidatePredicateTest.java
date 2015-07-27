package org.ovirt.engine.core.bll.network.dc.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.utils.linq.Predicate;

@RunWith(MockitoJUnitRunner.class)
public class ManagementNetworkCandidatePredicateTest {

    @Mock
    private Predicate<Network> mockExternalNetworkPredicate;

    @Mock
    private Network mockNetwork;

    private ManagementNetworkCandidatePredicate underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new ManagementNetworkCandidatePredicate(mockExternalNetworkPredicate);
    }

    @Test
    public void testEvalNegative() {
        when(mockExternalNetworkPredicate.eval(mockNetwork)).thenReturn(true);

        assertFalse(underTest.eval(mockNetwork));
    }

    @Test
    public void testEvalPositive() {
        when(mockExternalNetworkPredicate.eval(mockNetwork)).thenReturn(false);

        assertTrue(underTest.eval(mockNetwork));
    }
}
