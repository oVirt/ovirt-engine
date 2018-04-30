package org.ovirt.engine.core.bll.network.dc.predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.Network;

@ExtendWith(MockitoExtension.class)
public class ManagementNetworkCandidatePredicateTest {

    @Spy
    private Predicate<Network> mockExternalNetworkPredicate;

    @Mock
    private Network mockNetwork;

    private ManagementNetworkCandidatePredicate underTest;

    @BeforeEach
    public void setUp() {
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
