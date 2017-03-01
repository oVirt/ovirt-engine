package org.ovirt.engine.core.bll.network.dc.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;

@RunWith(MockitoJUnitRunner.class)
public class ManagementNetworkCandidatePredicateTest {

    @Spy
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
        doReturn(true).when(mockExternalNetworkPredicate).test(mockNetwork);
        assertFalse(underTest.test(mockNetwork));
    }

    @Test
    public void testEvalPositive() {
        doReturn(false).when(mockExternalNetworkPredicate).test(mockNetwork);
        assertTrue(underTest.test(mockNetwork));
    }
}
