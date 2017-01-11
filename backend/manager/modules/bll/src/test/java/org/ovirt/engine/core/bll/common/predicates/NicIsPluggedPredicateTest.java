package org.ovirt.engine.core.bll.common.predicates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class NicIsPluggedPredicateTest {

    @Mock
    private VmNetworkInterface mockVmNetworkInterface;

    @Test
    public void testEvalPositive() throws Exception {

        when(mockVmNetworkInterface.isPlugged()).thenReturn(true);

        assertTrue(NicIsPluggedPredicate.getInstance().test(mockVmNetworkInterface));

        verify(mockVmNetworkInterface).isPlugged();
        verify(mockVmNetworkInterface, never()).isLinked();
    }

    @Test
    public void testEvalNegative() throws Exception {

        when(mockVmNetworkInterface.isPlugged()).thenReturn(false);

        assertFalse(NicIsPluggedPredicate.getInstance().test(mockVmNetworkInterface));

        verify(mockVmNetworkInterface).isPlugged();
        verify(mockVmNetworkInterface, never()).isLinked();
    }
}
