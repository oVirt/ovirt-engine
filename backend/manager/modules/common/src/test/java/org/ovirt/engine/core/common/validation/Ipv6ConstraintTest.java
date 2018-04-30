package org.ovirt.engine.core.common.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.validation.annotation.Ipv6;

public class Ipv6ConstraintTest {

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
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<TestObject>> validationResult = validator.validate(new TestObject(input));
        boolean valid = validationResult.isEmpty();

        assertThat(valid, is(expectedResult));
    }

    public static class TestObject {
        @Ipv6
        private final String address;

        public TestObject(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }
    }

}
