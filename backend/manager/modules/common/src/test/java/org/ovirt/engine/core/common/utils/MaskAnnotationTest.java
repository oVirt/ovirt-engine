package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.annotation.Mask;

public class MaskAnnotationTest {
    private Validator validator;

    @BeforeEach
    public void setup() {
        validator = ValidationUtils.getValidator();
    }

    @ParameterizedTest
    @MethodSource("namesParams")
    public void checkCidrFormatAnnotation(String mask, boolean isValidMaskFormat, boolean isValidMaskValue) {
        MaskContainer container = new MaskContainer(mask);
        Set<ConstraintViolation<MaskContainer>> result = validator.validate(container);
        if (!isValidMaskValue && isValidMaskFormat) {
            assertEquals(EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name(),
                    result.iterator().next().getMessage(),
                    "Failed to validate mask's error format: " + container.getMask());
        } else if (!isValidMaskFormat) {
            assertEquals(EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name(),
                    result.iterator().next().getMessage(),
                    "Failed to validate mask's error format: " + container.getMask());
        } else {
            assertEquals(isValidMaskFormat, result.isEmpty(),
                    "Failed to validate mask's format: " + container.getMask());
        }

    }

    @ParameterizedTest
    @MethodSource("namesParams")
    public void checkCidrNetworkAddressAnnotation(String mask, boolean isValidMaskFormat, boolean isValidMaskValue) {
        MaskContainer container = new MaskContainer(mask);
        Set<ConstraintViolation<MaskContainer>> result = validator.validate(container);
        if (!isValidMaskFormat) {
            assertEquals(EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name(),
                    result.iterator().next().getMessage(),
                    "Failed to validate mask's network address error: " + container.getMask());
        } else if (!isValidMaskValue) {
            assertEquals(EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name(),
                    result.iterator().next().getMessage(),
                    "Failed to validate mask's  network address error: " + container.getMask());
        } else {
            assertEquals(isValidMaskValue, result.isEmpty(),
                    "Failed to validate mask's network address: " + container.getMask());
        }

    }

    public static Stream<Arguments> namesParams() {
        return Stream.of(
                // Bad Format
                Arguments.of("a.a.a.a", false, false),
                Arguments.of("//32", false, false),
                Arguments.of("33", false, false),

                // Not A Valid Mask
                Arguments.of("253.0.0.32", true, false),

                // valid mask
                Arguments.of("255.255.255.0", true, true),
                Arguments.of("15", true, true)
        );
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
