package org.ovirt.engine.core.bll.common.predicates;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;

@RunWith(MockitoJUnitRunner.class)
public class RunningVmPredicateTest {

    @Mock
    private VM mockVm;

    @Test
    public void testApplyPositive() {
        doTest(true);
    }

    @Test
    public void testApplyNegative() {
        doTest(false);
    }

    private void doTest(Boolean expectedResult) {
        final RunningVmPredicate underTest = new RunningVmPredicate();

        when(mockVm.isRunning()).thenReturn(expectedResult);

        final boolean actual = underTest.test(mockVm);

        verify(mockVm).isRunning();

        assertThat(actual, is(expectedResult));
    }
}
