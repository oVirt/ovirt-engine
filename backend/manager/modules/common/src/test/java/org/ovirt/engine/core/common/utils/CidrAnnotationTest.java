package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.annotation.Cidr;

@RunWith(Parameterized.class)
public class CidrAnnotationTest {

    private final String cidr;
    private final boolean validCidrFormatExpectedResult;
    private final boolean validNetworkAddressExpectedResult;
    private Validator validator;

    public CidrAnnotationTest(String cidr,
            boolean validCidrFormatExpectedResult,
            boolean validNetworkAddressExpectedResult) {
        this.cidr = cidr;
        this.validCidrFormatExpectedResult = validCidrFormatExpectedResult;
        this.validNetworkAddressExpectedResult = validNetworkAddressExpectedResult;
    }

    @Before
    public void setup() throws Exception {
        validator = ValidationUtils.getValidator();
    }

    @Test
    public void checkCidrFormatAnnotation() {
        CidrContainer container = new CidrContainer(cidr);
        Set<ConstraintViolation<CidrContainer>> result = validator.validate(container);
        if (!validNetworkAddressExpectedResult && validCidrFormatExpectedResult) {
            assertEquals("Failed to validate CIDR's error format: " + container.getCidr(),
                    EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name(),
                    result.iterator().next().getMessage());
        } else if (!validCidrFormatExpectedResult) {
            assertEquals("Failed to validate CIDR's error format: " + container.getCidr(),
                    EngineMessage.BAD_CIDR_FORMAT.name(),
                    result.iterator().next().getMessage());
        } else {
            assertEquals("Failed to validate CIDR's format: " + container.getCidr(),
                    validCidrFormatExpectedResult,
                    result.isEmpty());
        }

    }

    @Test
    public void checkCidrNetworkAddressAnnotation() {
        CidrContainer container = new CidrContainer(cidr);
        Set<ConstraintViolation<CidrContainer>> result = validator.validate(container);
        if (!validCidrFormatExpectedResult) {
            assertEquals("Failed to validate CIDR's network address error: " + container.getCidr(),
                    EngineMessage.BAD_CIDR_FORMAT.name(),
                    result.iterator().next().getMessage());
        } else if (!validNetworkAddressExpectedResult) {
            assertEquals("Failed to validate CIDR's  network address error: " + container.getCidr(),
                    EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name(),
                    result.iterator().next().getMessage());
        } else {
            assertEquals("Failed to validate CIDR's network address: " + container.getCidr(),
                    validNetworkAddressExpectedResult,
                    result.isEmpty());
        }

    }

    @Parameterized.Parameters
    public static Collection<Object[]> namesParams() {

        return Arrays.asList(new Object[][] {
                // Bad Format
                { "a.a.a.a", false, false },

                // Not A Network address
                { "253.0.0.32/26", true, false },

                // valid CIDR
                { "255.255.255.255/32", true, true },
        });

    }

    private static class CidrContainer {
        @Cidr
        private final String cidr;

        public CidrContainer(String cidr) {
            this.cidr = cidr;
        }

        public String getCidr() {
            return cidr;
        }

    }

}
