package org.ovirt.engine.core.utils.linq;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotPredicateTest {

    private static final Object testObject = new Object();

    private NotPredicate<Object> underTest;

    @Mock
    private Predicate<Object> mockPredicate;

    @Before
    public void setUp() throws Exception {
        underTest = new NotPredicate<>(mockPredicate);
    }

    @Test
    public void testEvalPositive() {
        when(mockPredicate.eval(testObject)).thenReturn(false);
        assertTrue(underTest.eval(testObject));
    }

    @Test
    public void testEvalNegative() {
        when(mockPredicate.eval(testObject)).thenReturn(true);
        assertFalse(underTest.eval(testObject));
    }
}
