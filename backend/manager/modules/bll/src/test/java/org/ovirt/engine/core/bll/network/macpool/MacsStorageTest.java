package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.math.LongRange;
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;

public class MacsStorageTest {

    private static final int RANGE_FROM = 10;
    private static final int RANGE_TO = 19;
    private static final int NUMBER_OF_MACS = 12;
    private static final boolean ALLOW_DUPLICATES = true;

    @Test
    public void rangesAltersInServingRequestsWhenLastRangeHasAvailableMac() {
        assertRangeAltering(5, Arrays.asList(1, 3), Arrays.asList(0, 2, 4, 0, 2, 4));
    }

    @Test
    public void rangesAltersInServingRequestsWhenLastRangeHasNotAvailableMac() {
        assertRangeAltering(5, Arrays.asList(0, 3, 4), Arrays.asList(1, 2, 1, 2));
    }

    private void assertRangeAltering(int numberOfRanges,
            List<Integer> rangesWithoutAvailableMacs, List<Integer> expectedRangeIndices) {
        List<Range> ranges = createMockedRanges(numberOfRanges);
        mockAvailableMacsInRange(ranges, rangesWithoutAvailableMacs);

        MacsStorage macsStorage = new MacsStorage(false);
        addRangesToStorage(ranges, macsStorage);

        assertReturnedRange(ranges, macsStorage, expectedRangeIndices);
    }

    @Test
    public void testNoDuplicateCustomMacs() {
        MacsStorage macsStorage = new MacsStorage(false);
        assertThat(macsStorage.containsDuplicates(), is(false));
        macsStorage.useMac(1L);
        assertThat(macsStorage.containsDuplicates(), is(false));
        macsStorage.useMac(1L);
        assertThat(macsStorage.containsDuplicates(), is(false));
    }

    @Test
    public void testDuplicateCustomMacs() {
        MacsStorage macsStorage = new MacsStorage(true);
        assertThat(macsStorage.containsDuplicates(), is(false));
        macsStorage.useMac(1L);
        assertThat(macsStorage.containsDuplicates(), is(false));
        macsStorage.useMac(1L);
        assertThat(macsStorage.containsDuplicates(), is(true));
        macsStorage.freeMac(1L);
        assertThat(macsStorage.containsDuplicates(), is(false));
    }

    private void assertReturnedRange(List<Range> ranges, MacsStorage macsStorage, List<Integer> expectedRangeIndices) {

        for (int expectedRangeIndex : expectedRangeIndices) {
            Range actualRange = macsStorage.getRangeWithAvailableMac();
            Range expectedRange = ranges.get(expectedRangeIndex);
            assertThat(actualRange, new IsSame<>(expectedRange));
        }
    }

    private void addRangesToStorage(List<Range> ranges, MacsStorage macsStorage) {
        for (Range range : ranges) {
            macsStorage.addRange(range);
        }
    }

    private void mockAvailableMacsInRange(List<Range> ranges, List<Integer> rangesWithoutAvailableMacs) {
        for(int i = 0; i < ranges.size(); i++) {
            Range range = ranges.get(i);
            boolean shouldBeEmpty = rangesWithoutAvailableMacs.contains(i);
            when(range.getAvailableCount()).thenReturn(shouldBeEmpty ? 0 : 1);
        }
    }

    private List<Range> createMockedRanges(int count) {
        List<Range> result = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            result.add(mock(Range.class));
        }
        return result;
    }

    @ParameterizedTest
    @MethodSource
    void testNoMacsLeftToAllocate(boolean allowDuplicates) {
        MacsStorage macsStorage = new MacsStorage(allowDuplicates);
        Range range = new Range(new LongRange(RANGE_FROM, RANGE_TO));
        macsStorage.addRange(range);
        EngineException e = assertThrows(EngineException.class, () -> macsStorage.allocateAvailableMacs(NUMBER_OF_MACS));
        assertTrue(e.getMessage().contains(EngineError.MAC_POOL_NO_MACS_LEFT.toString()));
    }

    static Stream<Object[]> testNoMacsLeftToAllocate() {
        return Stream.of(
                new Object[] { ALLOW_DUPLICATES },
                new Object[] { !ALLOW_DUPLICATES }
        );
    }
}
