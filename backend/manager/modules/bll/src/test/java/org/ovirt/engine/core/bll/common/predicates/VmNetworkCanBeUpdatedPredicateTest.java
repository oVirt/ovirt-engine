package org.ovirt.engine.core.bll.common.predicates;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class VmNetworkCanBeUpdatedPredicateTest {

    @Mock
    private VmNetworkInterface mockVmNetworkInterface;

    @Test
    public void testEvalPositive() throws Exception {

        Mockito.when(mockVmNetworkInterface.isPlugged()).thenReturn(true);

        Assert.assertTrue(VmNetworkCanBeUpdatedPredicate.getInstance().test(mockVmNetworkInterface));

        Mockito.verify(mockVmNetworkInterface).isPlugged();
        Mockito.verify(mockVmNetworkInterface, Mockito.never()).isLinked();
    }

    @Test
    public void testEvalNegative() throws Exception {

        Mockito.when(mockVmNetworkInterface.isPlugged()).thenReturn(false);

        Assert.assertFalse(VmNetworkCanBeUpdatedPredicate.getInstance().test(mockVmNetworkInterface));

        Mockito.verify(mockVmNetworkInterface).isPlugged();
        Mockito.verify(mockVmNetworkInterface, Mockito.never()).isLinked();
    }
}
