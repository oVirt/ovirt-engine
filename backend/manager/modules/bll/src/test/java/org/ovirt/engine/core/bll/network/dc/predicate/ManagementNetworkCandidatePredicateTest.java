package org.ovirt.engine.core.bll.network.dc.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.utils.DummyPredicate;

@RunWith(MockitoJUnitRunner.class)
public class ManagementNetworkCandidatePredicateTest {

    private DummyPredicate<Network> mockExternalNetworkPredicate = new DummyPredicate<>();

    @Mock
    private Network mockNetwork;

    private ManagementNetworkCandidatePredicate underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new ManagementNetworkCandidatePredicate(mockExternalNetworkPredicate);
    }

    @Test
    public void testEvalNegative() {
        mockExternalNetworkPredicate.setTestResult(true);
        assertFalse(underTest.test(mockNetwork));
    }

    @Test
    public void testEvalPositive() {
        mockExternalNetworkPredicate.setTestResult(false);
        assertTrue(underTest.test(mockNetwork));
    }
}
