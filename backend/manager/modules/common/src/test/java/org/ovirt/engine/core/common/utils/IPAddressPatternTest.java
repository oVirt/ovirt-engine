package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class IPAddressPatternTest {

    private Validator validator;
    private String address;
    private boolean expectedResult;

    public IPAddressPatternTest(String address, Boolean expectedResult) {
        this.address = address;
        this.expectedResult = expectedResult;
        validator = ValidationUtils.getValidator();
    }

    @Test
    public void checkIPAdress() {
        Set<ConstraintViolation<IPAdress>> validate = validator.validate(new IPAdress(address));
        assertEquals(expectedResult, validate.isEmpty());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> ipAddressParams() {
        return Arrays.asList(new Object[][] {
                { "0.0.0.0", true },
                { "1.1.1.1", true },
                { "255.255.255.255", true },
                { "192.168.1.1", true },
                { "10.10.1.1", true },
                { "127.0.0.1", true },
                { "", true },
                { null, true },
                { "10.10.10", false },
                { "10.10", false },
                { "10", false },
                { "1.1.1.", false },
                { "1.1..1", false },
                { "1..1.1", false },
                { ".1.1.1", false },
                { "....", false },
                { "...", false },
                { "..", false },
                { ".", false },
                { "1.1.1.1.1", false },
                { "a.10.10.10", false },
                { "10.a.10.10", false },
                { "10.10.a.10", false },
                { "10.10.10.a", false },
                { "a.a.a.a", false },
                { "256.10.10.10", false },
                { "10.256.10.10", false },
                { "10.10.256.10", false },
                { "10.10.10.256", false },
                { "300.10.10.10", false },
                { "10.300.10.10", false },
                { "10.10.300.10", false },
                { "10.10.10.300", false },
                { "-1.10.10.10", false },
                { "10.-1.10.10", false },
                { "10.10.-1.10", false },
                { "10.10.10.-1", false },
                { " ", false },
        });
    }

    private static class IPAdress {

        @Pattern(regexp = ValidationUtils.IPV4_PATTERN, message = "IPV4_ADDR_BAD_FORMAT")
        private String address;

        public IPAdress(String address) {
            this.address = address;
        }
    }

}
