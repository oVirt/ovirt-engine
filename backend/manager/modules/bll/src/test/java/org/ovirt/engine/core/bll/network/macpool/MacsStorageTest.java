package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MacAddressRangeUtils.macToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.math.LongRange;
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;

public class MacsStorageTest {

    private static final int RANGE_FROM = 10;
    private static final int RANGE_TO = 19;
    private static final int NUMBER_OF_MACS = 12;
    private static final boolean ALLOW_DUPLICATES = true;
    private static final List<Integer> RANGE_BOUNDARIES =Arrays.asList(
        RANGE_FROM,
        RANGE_FROM + NUMBER_OF_MACS / 3,
        RANGE_FROM + NUMBER_OF_MACS / 3 * 2,
        RANGE_FROM + NUMBER_OF_MACS
    );

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

        MacsStorage macsStorage = new MacsStorage(false, skip -> false);
        addRangesToStorage(ranges, macsStorage);

        assertReturnedRange(ranges, macsStorage, expectedRangeIndices);
    }

    @Test
    public void testNoDuplicateCustomMacs() {
        MacsStorage macsStorage = new MacsStorage(false, skip -> false);
        assertThat(macsStorage.containsDuplicates(), is(false));
        macsStorage.useMac(1L);
        assertThat(macsStorage.containsDuplicates(), is(false));
        macsStorage.useMac(1L);
        assertThat(macsStorage.containsDuplicates(), is(false));
    }

    @Test
    public void testDuplicateCustomMacs() {
        MacsStorage macsStorage = new MacsStorage(true, skip -> false);
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
        MacsStorage macsStorage = new MacsStorage(allowDuplicates, skip -> false);
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

    @ParameterizedTest
    @MethodSource
    void testAllocateMacs(int macsToAllocate, boolean allowDuplicates, int macsInUse, int expectedAllocated) {
        List<Range> ranges = createRanges();
        final Map<String, Boolean> predicateMap = roundRobinDistributeMacsInUseToRanges(macsInUse, ranges, allowDuplicates);
        MacsStorage macsStorage = Mockito.spy(new MacsStorage(allowDuplicates, predicateMap::get));
        doNothing().when(macsStorage).auditAllocatingMacsInUse(anyInt());
        ranges.forEach(macsStorage::addRange);
        List<Long> macs = macsStorage.allocateAvailableMacs(macsToAllocate);
        assertEquals(expectedAllocated, macs.size());
    }

    private List<Range> createRanges() {
        List<Range> ranges = new ArrayList<>(RANGE_BOUNDARIES.size() - 1);
        IntStream.range(0, RANGE_BOUNDARIES.size() - 1).forEach(idx -> ranges.add(
            new Range(new LongRange((int) RANGE_BOUNDARIES.get(idx), RANGE_BOUNDARIES.get(idx + 1) - 1)))
        );
        return ranges;
    }

    private Map<String, Boolean> roundRobinDistributeMacsInUseToRanges(int countMacsInUse, List<Range> ranges, boolean allowDuplicates) {
        Map<String, Boolean> predicateMap = new HashMap<>(NUMBER_OF_MACS);
        for (int count = 0; count < NUMBER_OF_MACS; count++) {
            int rangeIndex = count % ranges.size();
            int inRangeMacIndex = (count - rangeIndex) / ranges.size();
            boolean v = !allowDuplicates && countMacsInUse > count;
            predicateMap.put(macToString(RANGE_BOUNDARIES.get(rangeIndex) + inRangeMacIndex), v);
        }
        return predicateMap;
    }

    static Stream<Object[]> testAllocateMacs() {
        return Stream.of(
                // macsToAllocate, allowDuplicates, numMacsInUse (elsewhere), expectedAllocated

                // fully  allocate - duplicates allowed
                new Object[] { 0, ALLOW_DUPLICATES, NUMBER_OF_MACS, 0 },
                new Object[] { 1, ALLOW_DUPLICATES, NUMBER_OF_MACS, 1 },
                new Object[] { NUMBER_OF_MACS, ALLOW_DUPLICATES, NUMBER_OF_MACS, NUMBER_OF_MACS },

                new Object[] { 0, ALLOW_DUPLICATES, 0, 0 },
                new Object[] { 1, ALLOW_DUPLICATES, 0, 1 },
                new Object[] { NUMBER_OF_MACS, ALLOW_DUPLICATES, 0, NUMBER_OF_MACS },

                // fully allocate - nothing in use
                new Object[] { 0, !ALLOW_DUPLICATES, 0, 0 },
                new Object[] { 1, !ALLOW_DUPLICATES, 0, 1 },
                new Object[] { NUMBER_OF_MACS, !ALLOW_DUPLICATES, 0, NUMBER_OF_MACS },

                // fully allocate - requested allocation small enough
                new Object[] { 1, !ALLOW_DUPLICATES, 1, 1 },
                new Object[] { NUMBER_OF_MACS - 1, !ALLOW_DUPLICATES, 1, NUMBER_OF_MACS - 1 },
                new Object[] { 1, !ALLOW_DUPLICATES, NUMBER_OF_MACS - 1, 1 },

                // some in use - - allocate unused and then used
                new Object[] { NUMBER_OF_MACS, !ALLOW_DUPLICATES, 1, NUMBER_OF_MACS },
                new Object[] { NUMBER_OF_MACS, !ALLOW_DUPLICATES, NUMBER_OF_MACS / 3, NUMBER_OF_MACS },
                new Object[] { NUMBER_OF_MACS, !ALLOW_DUPLICATES, NUMBER_OF_MACS / 2, NUMBER_OF_MACS },

                // all in use - allocate used
                new Object[] { 0, !ALLOW_DUPLICATES, NUMBER_OF_MACS, 0 },
                new Object[] { 1, !ALLOW_DUPLICATES, NUMBER_OF_MACS, 1 },
                new Object[] { NUMBER_OF_MACS, !ALLOW_DUPLICATES, NUMBER_OF_MACS, NUMBER_OF_MACS },
                new Object[] { NUMBER_OF_MACS, !ALLOW_DUPLICATES, NUMBER_OF_MACS, NUMBER_OF_MACS }
        );
    }
}
