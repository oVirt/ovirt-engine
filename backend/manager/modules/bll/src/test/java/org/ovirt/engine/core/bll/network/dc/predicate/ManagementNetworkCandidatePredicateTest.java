package org.ovirt.engine.core.bll.network.dc.predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.Network;

@ExtendWith(MockitoExtension.class)
public class ManagementNetworkCandidatePredicateTest {

    @Mock
    private Network mockNetwork;

    private ManagementNetworkCandidatePredicate underTest;

    @BeforeEach
    public void setUp() {
        Predicate<Network> externalNetworkPredicate = network -> network.isExternal();
        underTest = new ManagementNetworkCandidatePredicate(externalNetworkPredicate);
    }

    @Test
    public void testEvalNegative() {
        when(mockNetwork.isExternal()).thenReturn(true);
        assertFalse(underTest.test(mockNetwork));
    }

    @Test
    public void testEvalPositive() {
        when(mockNetwork.isExternal()).thenReturn(false);
        assertTrue(underTest.test(mockNetwork));
    }
}
