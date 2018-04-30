package org.ovirt.engine.core.common.businessentities.comparators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LexoNumericComparatorTest {
    private void verifyResult(LexoNumericComparator comparator, String left, String right, int expectedResult) {
        assertEquals(expectedResult, comparator.compare(left, right),
                String.format("Expected %1$s to be %3$s %2$s, but it wasn't.",
                        left,
                        right,
                        expectedResult == -1 ? "less than" : expectedResult == 1 ? "greater than" : "equal to"));
    }

    @ParameterizedTest
    @MethodSource
    public void comparison(boolean caseSensitive, String left, String right, int expectedResult) {
        verifyResult(new LexoNumericComparator(caseSensitive), left, right, expectedResult);
        verifyResult(new LexoNumericComparator(caseSensitive), right, left, -expectedResult);
    }

    public static Stream<Arguments> comparison() {
        return Stream.of(
                Arguments.of(false, null, null, 0),
                Arguments.of(false, null, "", -1),
                Arguments.of(false, "", "", 0),
                Arguments.of(false, "", "123", -1),
                Arguments.of(false, "123", "123", 0),
                Arguments.of(false, "123", "456", -1),
                Arguments.of(false, "12", "123", -1),
                Arguments.of(false, "012", "12", -1),
                Arguments.of(false, "2", "10", -1),
                Arguments.of(false, "123abc", "123abc", 0),
                Arguments.of(true, "123abc", "123abc", 0),
                Arguments.of(false, "123Abc", "123abc", -1),
                Arguments.of(true, "123Abc", "123abc", -1),
                Arguments.of(false, "123abc", "456abc", -1),
                Arguments.of(false, "12abc", "123abc", -1),
                Arguments.of(false, "012abc", "12abc", -1),
                Arguments.of(false, "2abc", "10abc", -1),
                Arguments.of(false, "123", "abc", -1),
                Arguments.of(false, "abc", "abc", 0),
                Arguments.of(true, "abc", "abc", 0),
                Arguments.of(false, "Abc", "abc", -1),
                Arguments.of(true, "Abc", "abc", -1),
                Arguments.of(false, "abc", "def", -1),
                Arguments.of(false, "ab", "abc", -1),
                Arguments.of(false, "123abc", "123def", -1),
                Arguments.of(false, "123ab", "123abc", -1),
                Arguments.of(false, "abc123", "abc123", 0),
                Arguments.of(true, "abc123", "abc123", 0),
                Arguments.of(false, "Abc123", "abc123", -1),
                Arguments.of(true, "Abc123", "abc123", -1),
                Arguments.of(false, "Abc234", "abc123", 1),
                Arguments.of(true, "Abc234", "abc123", -1),
                Arguments.of(false, "Abc1234", "abc123", 1),
                Arguments.of(true, "Abc1234", "abc123", -1),
                Arguments.of(false, "abc123", "def123", -1),
                Arguments.of(false, "ab123", "abc123", -1),
                Arguments.of(false, "abc123", "abc456", -1),
                Arguments.of(false, "abc12", "abc123", -1),
                Arguments.of(false, "abc012", "abc12", -1),
                Arguments.of(false, "abc2", "abc10", -1),
                Arguments.of(false, "abc123def", "abc123def", 0),
                Arguments.of(true, "abc123def", "abc123def", 0),
                Arguments.of(false, "Abc123def", "abc123def", -1),
                Arguments.of(true, "Abc123def", "abc123def", -1),
                Arguments.of(false, "Abc234def", "abc123def", 1),
                Arguments.of(true, "Abc234def", "abc123def", -1),
                Arguments.of(false, "Abc1234def", "abc123def", 1),
                Arguments.of(true, "Abc1234def", "abc123def", -1),
                Arguments.of(false, "abc123def", "abc123ghi", -1),
                Arguments.of(false, "abc123de", "abc123def", -1),
                Arguments.of(false, "abc123456789123de", "abc123456789123de", 0),
                Arguments.of(true, "abc123456789123de", "abc123456789123de", 0),
                Arguments.of(false, "Abc123456789123de", "abc123456789123de", -1),
                Arguments.of(true, "Abc123456789123de", "abc123456789123de", -1),
                Arguments.of(false, "Abc123456789234de", "abc123456789123de", 1),
                Arguments.of(true, "Abc123456789234de", "abc123456789123de", -1),
                Arguments.of(false, "Abc1234567891234de", "abc123456789123de", 1),
                Arguments.of(true, "Abc1234567891234de", "abc123456789123de", -1),
                Arguments.of(false, "abc123456789123de", "abc123456789123fg", -1),
                Arguments.of(false, "abc123456789123de", "abc123456789123def", -1)
        );
    }
}
