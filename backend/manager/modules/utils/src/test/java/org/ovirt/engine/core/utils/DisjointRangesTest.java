package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.LongRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(RandomUtilsSeedingExtension.class)
public class DisjointRangesTest {
    public static Stream<Arguments> ranges() {
        return Stream.of(
                Arguments.of(Collections.<LongRange>emptyList(), Collections.<LongRange>emptyList()),
                Arguments.of(Collections.singletonList(pair(1, 2)), Collections.singletonList(pair(1, 2))),
                Arguments.of(Arrays.asList(pair(1, 2), pair(3, 4)), Arrays.asList(pair(1, 2), pair(3, 4))),
                Arguments.of(Arrays.asList(pair(1, 3), pair(2, 5)), Collections.singletonList(pair(1, 5))),
                Arguments.of(Arrays.asList(pair(1, 3), pair(5, 7), pair(6, 7)), Arrays.asList(pair(1, 3), pair(5, 7))),
                Arguments.of(Arrays.asList(pair(1, 2), pair(5, 7), pair(4, 7)), Arrays.asList(pair(1, 2), pair(4, 7))),
                Arguments.of(Arrays.asList(pair(1, 3), pair(5, 7), pair(4, 7)), Arrays.asList(pair(1, 3), pair(4, 7))),
                Arguments.of(Arrays.asList(pair(1, 2), pair(5, 6), pair(9, 11), pair(3, 11)), Arrays.asList(pair(1, 2), pair(3, 11))),
                Arguments.of(Arrays.asList(pair(1, 2), pair(5, 6), pair(9, 11), pair(3, 12)), Arrays.asList(pair(1, 2), pair(3, 12))),
                Arguments.of(Arrays.asList(pair(1, 2), pair(5, 6), pair(9, 11), pair(3, 8)), Arrays.asList(pair(1, 2), pair(3, 8), pair(9, 11))),
                Arguments.of(Arrays.asList(pair(1, 3), pair(5, 6), pair(9, 11), pair(-1, 8)), Arrays.asList(pair(-1, 8), pair(9, 11)))
        );
    }

    private static LongRange pair(long from, long to) {
        return new LongRange(from, to);
    }

    @ParameterizedTest
    @MethodSource
    public void ranges(List<LongRange> inputRanges, List<LongRange> expectedRanges) {
        Collections.shuffle(inputRanges, RandomUtils.instance());
        DisjointRanges disjointRanges = new DisjointRanges();
        disjointRanges.addRanges(inputRanges);

        Collection<LongRange> result = disjointRanges.getRanges();

        assertTrue(CollectionUtils.isEqualCollection(expectedRanges, result));
    }
}
