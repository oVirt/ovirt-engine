package org.ovirt.engine.core.bll.snapshots;


import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CountMacUsageDifferenceTest {
    public static Stream<Arguments> data() {
        return Stream.of(
            //                 from                to              extraMacs      missingMacs     toMacsAlreadyAllocated
            Arguments.of(stringsRange(1, 3), stringsRange(2, 4), stringsFrom(1), stringsFrom(4), false),
            Arguments.of(stringsRange(1, 1), stringsRange(1, 1), stringsFrom(), stringsFrom(), false),
            Arguments.of(stringsRange(1, 1), stringsRange(2, 2), stringsFrom(1), stringsFrom(2), false),
            Arguments.of(stringsRange(2, 2), stringsRange(1, 1), stringsFrom(2), stringsFrom(1), false),

            Arguments.of(stringsFrom(1, 1, 1, 2), stringsFrom(1, 1, 2, 2, 2), stringsFrom(1), stringsFrom(2, 2), false),

            Arguments.of(stringsFrom(1, null), stringsFrom(1, null), stringsFrom(), stringsFrom(), false),



            Arguments.of(stringsRange(1, 3), stringsRange(2, 4), stringsFrom(1), stringsFrom(), true),
            Arguments.of(stringsRange(1, 1), stringsRange(1, 1), stringsFrom(), stringsFrom(), true),
            Arguments.of(stringsRange(1, 1), stringsRange(2, 2), stringsFrom(1), stringsFrom(), true),
            Arguments.of(stringsRange(2, 2), stringsRange(1, 1), stringsFrom(2), stringsFrom(), true),

            Arguments.of(stringsFrom(1, 1, 1, 2), stringsFrom(1, 1, 2, 2, 2), stringsFrom(1), stringsFrom(), true),

            Arguments.of(stringsFrom(1, null), stringsFrom(1, null), stringsFrom(), stringsFrom(), true)
        );
    }

    private static List<String> stringsFrom(Integer ... strings) {
        return Arrays.stream(strings).map(e -> e == null ? null : e.toString()).collect(Collectors.toList());
    }

    private static List<String> stringsRange(int from, int to) {
        return IntStream.rangeClosed(from, to).boxed().map(Object::toString).collect(Collectors.toList());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testExtraMacs(
            List<String> from,
            List<String> to,
            List<String> extraMacs,
            List<String> missingMacs,
            boolean toMacsAreExpectedToBeAlreadyAllocated) {

        CountMacUsageDifference countMacUsageDifference =
                new CountMacUsageDifference(from.stream(), to.stream(), toMacsAreExpectedToBeAlreadyAllocated);
        assertThat(countMacUsageDifference.getExtraMacs(), CoreMatchers.equalTo(extraMacs));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testMissingMacs(
            List<String> from,
            List<String> to,
            List<String> extraMacs,
            List<String> missingMacs,
            boolean toMacsAreExpectedToBeAlreadyAllocated) {

        CountMacUsageDifference countMacUsageDifference =
                new CountMacUsageDifference(from.stream(), to.stream(), toMacsAreExpectedToBeAlreadyAllocated);
        assertThat(countMacUsageDifference.getMissingMacs(), CoreMatchers.equalTo(missingMacs));
    }
}
