package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkLabelFormat;

public class NetworkLabelFormatValidatorTest {

    private Validator validator = ValidationUtils.getValidator();

    @ParameterizedTest
    @MethodSource
    public void checkNetworkLabelFormat(Set<String> labels, boolean expectedResult) {
        NetworkLabelContainer labelContainer = new NetworkLabelContainer(labels);
        Set<ConstraintViolation<NetworkLabelContainer>> validate = validator.validate(labelContainer);
        assertEquals(expectedResult, validate.isEmpty(), "Failed to validate " + labelContainer.getLabels());
    }

    public static Stream<Arguments> checkNetworkLabelFormat() {
        return Stream.of(
                Arguments.of(Collections.emptySet(), true ),
                Arguments.of(null, true ),
                Arguments.of(Collections.singleton("abc"), true ),
                Arguments.of(new HashSet<>(Arrays.asList("abc", "xyz")), true ),
                Arguments.of(Collections.singleton("abc-_sc"), true ),
                Arguments.of(Collections.singleton(""), false ),
                Arguments.of(Collections.singleton(" "), false ),
                Arguments.of(Collections.singleton("abc*"), false ),
                Arguments.of(new HashSet<>(Arrays.asList("aaa", "abc*")), false)
        );
    }

    private static class NetworkLabelContainer {
        @ValidNetworkLabelFormat
        private Set<String> labels;

        public NetworkLabelContainer(Set<String> labels) {
            this.labels = labels;
        }

        public Set<String> getLabels() {
            return labels;
        }
    }
}
