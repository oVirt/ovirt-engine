package org.ovirt.engine.core.config.entity.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MacAddressPoolRangesValueHelperTest {

    private MacAddressPoolRangesValueHelper validator = new MacAddressPoolRangesValueHelper();

    @ParameterizedTest
    @MethodSource
    public void ranges(String ranges, boolean expectedResult) {
        assertEquals(expectedResult, validator.validate(null, ranges).isOk());
    }

    public static Stream<Arguments> ranges() {
        return Stream.of(
                Arguments.of("00:00:00:00:00:00-00:00:00:00:00:FF", true),
                Arguments.of("00:1A:4A:16:88:FD-00:1A:4A:16:88:FD", true),
                Arguments.of("AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB", true),
                Arguments.of("AA:AA:AA:AA:AA:AA-aa:aa:aa:aa:aa:ab", true),
                Arguments.of("aa:aa:aa:aa:aa:aa-AA:AA:AA:AA:AA:AB", true),
                Arguments.of("AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB,AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB", true),
                Arguments.of("AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB,CC:CC:CC:CC:CC:CC-CC:CC:CC:CC:CC:CD", true),
                Arguments.of("CC:CC:CC:CC:CC:CC-CC:CC:CC:CC:CC:CD,AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB", true),
                Arguments.of("BB:BB:BB:BB:BB:BB-AA:AA:AA:AA:AA:AA", false),
                Arguments.of("BB:BB:BB:BB:BB:BB-aa:aa:aa:aa:aa:aa", false),
                Arguments.of("bb:bb:bb:bb:bb:bb-AA:AA:AA:AA:AA:AA", false),
                Arguments.of("BB:BB:BB:BB:BB:BA,BB:BB:BB:BB:BB:BB", false),
                Arguments.of("AA:AA:AA:AA:AA,BB:BB:BB:BB:BB:BB", false),
                Arguments.of("AA-AA-AA-AA-AA-AA-BB-BB-BB-BB-BB-BB", false),
                Arguments.of("AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB,XA:AA:AA:AA:AA:AA-BB:BB:BB:BB:BB:BB", false),
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of(" ", false)
        );
    }
}
