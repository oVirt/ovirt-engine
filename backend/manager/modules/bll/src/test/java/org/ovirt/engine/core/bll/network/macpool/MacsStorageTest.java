package org.ovirt.engine.core.bll.network.macpool;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.core.IsSame;
import org.junit.Test;

public class MacsStorageTest {

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
}
