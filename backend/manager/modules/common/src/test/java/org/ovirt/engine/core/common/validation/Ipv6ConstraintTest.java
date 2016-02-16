package org.ovirt.engine.core.common.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.validation.ConstraintValidatorContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Ipv6ConstraintTest {

    @Mock
    private ConstraintValidatorContext mockConstraintValidatorContext;

    private Ipv6Constraint underTest = new Ipv6Constraint();

    @Test
    public void testStandardIsValid() {
        doTest("0123:1234:7:8:12CD:0ABC:ABcd:cdef", true);
    }

    @Test
    public void testHexCompressedIsValid() {
        doTest("1234::cdef", true);
    }

    @Test
    public void testNullIsValid() {
        doTest(null, true);
    }

    @Test
    public void testIpv4IsInvalid() {
        doTest("1.2.3.4", false);
    }

    @Test
    public void testEmptyStringIsInvalid() {
        doTest("", false);
    }

    private void doTest(String input, boolean expectedResult) {
        final boolean actualResult = underTest.isValid(input, mockConstraintValidatorContext);
        assertThat(actualResult, is(expectedResult));
    }

}
