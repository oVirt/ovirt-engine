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
import org.ovirt.engine.core.common.validation.annotation.Mask;

@RunWith(Parameterized.class)
public class MaskAnnotationTest {

    private final String mask;
    private final boolean isValidMaskFormat;
    private final boolean isValidMaskValue;
    private Validator validator;

    public MaskAnnotationTest(String mask,
            boolean isValidMaskFormat,
            boolean isValidMaskValue) {
        this.mask = mask;
        this.isValidMaskFormat = isValidMaskFormat;
        this.isValidMaskValue = isValidMaskValue;
    }

    @Before
    public void setup() throws Exception {
        validator = ValidationUtils.getValidator();
    }

    @Test
    public void checkCidrFormatAnnotation() {
        MaskContainer container = new MaskContainer(mask);
        Set<ConstraintViolation<MaskContainer>> result = validator.validate(container);
        if (!isValidMaskValue && isValidMaskFormat) {
            assertEquals("Failed to validate mask's error format: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name(),
                    result.iterator().next().getMessage());
        } else if (!isValidMaskFormat) {
            assertEquals("Failed to validate mask's error format: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name(),
                    result.iterator().next().getMessage());
        } else {
            assertEquals("Failed to validate mask's format: " + container.getMask(),
                    isValidMaskFormat,
                    result.isEmpty());
        }

    }

    @Test
    public void checkCidrNetworkAddressAnnotation() {
        MaskContainer container = new MaskContainer(mask);
        Set<ConstraintViolation<MaskContainer>> result = validator.validate(container);
        if (!isValidMaskFormat) {
            assertEquals("Failed to validate mask's network address error: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name(),
                    result.iterator().next().getMessage());
        } else if (!isValidMaskValue) {
            assertEquals("Failed to validate mask's  network address error: " + container.getMask(),
                    EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name(),
                    result.iterator().next().getMessage());
        } else {
            assertEquals("Failed to validate mask's network address: " + container.getMask(),
                    isValidMaskValue,
                    result.isEmpty());
        }

    }

    @Parameterized.Parameters
    public static Collection<Object[]> namesParams() {

        return Arrays.asList(new Object[][] {
                // Bad Format
                { "a.a.a.a", false, false },
                { "//32", false, false },
                { "33", false, false },

                // Not A Valid Mask
                { "253.0.0.32", true, false },

                // valid mask
                { "255.255.255.0", true, true },
                { "15", true, true }
        });

    }

    private static class MaskContainer {
        @Mask
        private final String mask;

        public MaskContainer(String mask) {
            this.mask = mask;
        }

        public String getMask() {
            return mask;
        }

    }

}
