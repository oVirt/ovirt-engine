package org.ovirt.engine.core.common.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.validation.annotation.Ipv4OrIpv6;

public class Ipv4OrIpv6ConstraintTest {

    @Test
    public void testIpv4IsValid() {
        doTest("1.2.3.4", true);
    }

    @Test
    public void testNullIsValid() {
        doTest(null, true);
    }

    @Test
    public void testEmptyStringIsValid() {
        doTest("", false);
    }

    @Test
    public void testOtherTextThanIpv4IsInvalid() {
        doTest("nonsense", false);
    }

    @Test
    public void testIpv6IsValid() {
        doTest("0123:1234:7:8:12CD:0ABC:ABcd:cdef", true);
    }

    @Test
    public void testHexCompressedIsValid() {
        doTest("1234::cdef", true);
    }

    private void doTest(String input, boolean expectedResult) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<TestObject>> validationResult = validator.validate(new TestObject(input));
        boolean valid = validationResult.isEmpty();

        assertThat(valid, is(expectedResult));
    }

    public static class TestObject {
        @Ipv4OrIpv6
        private final String address;

        public TestObject(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }
    }

}
