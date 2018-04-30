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
import org.ovirt.engine.core.common.validation.annotation.ValidName;

public class ValidNameValidatorTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        validator = ValidationUtils.getValidator();
    }

    @ParameterizedTest
    @MethodSource
    public void checkName(String name, boolean expectedResult) {
        ValidNameContainer container = new ValidNameContainer(name);
        Set<ConstraintViolation<ValidNameContainer>> result = validator.validate(container);
        assertEquals(expectedResult, result.isEmpty(), "Failed to validate name: " + container.getName());
    }

    public static Stream<Arguments> checkName() {
        return Stream.of(
                Arguments.of("abc", true ),
                Arguments.of("123", true ),
                Arguments.of("abc123", true ),
                Arguments.of("123abc", true ),
                Arguments.of(null, true ),
                Arguments.of(" ", false ),
                Arguments.of("", false ),
                Arguments.of("abc ", false ),
                Arguments.of(" abc", false ),
                Arguments.of("abc cde", false ),
                Arguments.of("abc*", false)
        );
    }

    private static class ValidNameContainer {

        @ValidName
        private String name;

        public ValidNameContainer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
