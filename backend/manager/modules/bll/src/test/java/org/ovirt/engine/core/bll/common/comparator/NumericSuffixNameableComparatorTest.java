package org.ovirt.engine.core.bll.common.comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.Nameable;

public class NumericSuffixNameableComparatorTest {

    private NumericSuffixNameableComparator underTest;

    @BeforeEach
    public void setUp() {
        underTest = new NumericSuffixNameableComparator();
    }

    @ParameterizedTest
    @MethodSource
    public void compare(String name1, String name2, int expected) {
        Nameable nameable1 = () -> name1;
        Nameable nameable2 = () -> name2;
        Matcher<Integer> expectedMatcher;
        if (expected == 0) {
            expectedMatcher = is(0);
        } else if (expected > 0) {
            expectedMatcher = greaterThan(0);
        } else {
            expectedMatcher = lessThan(0);
        }
        assertThat(underTest.compare(nameable1, nameable2), expectedMatcher);
    }

    public static Stream<Arguments> compare()  {
        return Stream.of(
                Arguments.of(null, null, 0),
                Arguments.of(null, "", -1),
                Arguments.of("", "", 0),
                Arguments.of("", "123", -1),
                Arguments.of("123", "", 1),
                Arguments.of("123", "123", 0),
                Arguments.of("123", "1", 1),
                Arguments.of("1", "123", -1),
                Arguments.of("01", "0123", -1),
                Arguments.of("abc123", "123", 1),
                Arguments.of("abc123", "1", 1),
                Arguments.of("abc1", "123", 1),
                Arguments.of("abc01", "0123", 1),
                Arguments.of("123", "abc123", -1),
                Arguments.of("123", "abc1", -1),
                Arguments.of("1", "abc123", -1),
                Arguments.of("01", "abc0123", -1),
                Arguments.of("abc123", "abc123", 0),
                Arguments.of("abc123", "abc1", 1),
                Arguments.of("abc1", "abc123", -1),
                Arguments.of("abc01", "abc0123", -1),
                Arguments.of("abc", "abc123", -1),
                Arguments.of("abc123", "abc", 1)
        );
    }
}
