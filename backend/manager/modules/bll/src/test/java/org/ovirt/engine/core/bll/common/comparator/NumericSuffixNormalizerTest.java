package org.ovirt.engine.core.bll.common.comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.bll.common.NumericSuffixNormalizer;

public class NumericSuffixNormalizerTest {

    private NumericSuffixNormalizer underTest;

    @BeforeEach
    public void setUp() {
        underTest = new NumericSuffixNormalizer();
    }

    @ParameterizedTest
    @MethodSource
    public void normalization(String str1, String str2, String expected1, String expected2) {
        final List<String> actual = underTest.normalize(str1, str2);
        assertThat(actual.get(0), is(expected1));
        assertThat(actual.get(1), is(expected2));
    }

    public static Stream<Arguments> normalization() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(null, "", null, ""),
                Arguments.of("", "", "", ""),
                Arguments.of("", "123", "", "123"),
                Arguments.of("123", "", "123", ""),
                Arguments.of("123", "123", "123", "123"),
                Arguments.of("123", "1", "123", "001"),
                Arguments.of("1", "123", "001", "123"),
                Arguments.of("01", "0123", "0001", "0123"),
                Arguments.of("abc123", "123", "abc123", "123"),
                Arguments.of("abc123", "1", "abc123", "001"),
                Arguments.of("abc1", "123", "abc001", "123"),
                Arguments.of("abc01", "0123", "abc0001", "0123"),
                Arguments.of("123", "abc123", "123", "abc123"),
                Arguments.of("123", "abc1", "123", "abc001"),
                Arguments.of("1", "abc123", "001", "abc123"),
                Arguments.of("01", "abc0123", "0001", "abc0123"),
                Arguments.of("abc123", "abc123", "abc123", "abc123"),
                Arguments.of("abc123", "abc1", "abc123", "abc001"),
                Arguments.of("abc1", "abc123", "abc001", "abc123"),
                Arguments.of("abc01", "abc0123", "abc0001", "abc0123"),
                Arguments.of("abc", "abc123", "abc", "abc123"),
                Arguments.of("abc123", "abc", "abc123", "abc")
        );
    }
}
