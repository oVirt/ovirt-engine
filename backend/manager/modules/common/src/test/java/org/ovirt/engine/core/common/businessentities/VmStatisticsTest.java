package org.ovirt.engine.core.common.businessentities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class VmStatisticsTest {

    private VmStatistics statistics = new VmStatistics();

    @Test
    public void addToHistoryAddingNull() {
        assertEquals(Arrays.asList(1, 2), statistics.addToHistory(Arrays.asList(1, 2), null, 100));
    }

    @Test
    public void addToHistoryNullBefore() {
        assertEquals(Collections.singletonList(12), statistics.addToHistory(null, 12, 100));
    }

    @Test
    public void addToHistoryEmptyBefore() {
        assertEquals(Collections.singletonList(12), statistics.addToHistory(new ArrayList<Integer>(), 12, 100));
    }

    @Test
    public void addToHistoryOneValueBefore() {
        assertEquals(Arrays.asList(10, 12), statistics.addToHistory(Collections.singletonList(10), 12, 100));
    }

    @Test
    public void addToHistoryTwoValuesBefore() {
        assertEquals(Arrays.asList(10, 11, 12), statistics.addToHistory(Arrays.asList(10, 11), 12, 100));
    }

    @Test
    public void addToHistoryOneOverLimitValuesBefore() {
        assertEquals(Arrays.asList(11, 12, 13), statistics.addToHistory(Arrays.asList(10, 11, 12), 13, 3));
    }

    @Test
    public void addToHistoryTwoOverLimitValuesBefore() {
        assertEquals(Arrays.asList(11, 12, 13), statistics.addToHistory(Arrays.asList(9, 10, 11, 12), 13, 3));
    }

    @Test
    public void addToHistoryOneLimit() {
        assertEquals(Collections.singletonList(13), statistics.addToHistory(Arrays.asList(9, 10, 11, 12), 13, 1));
    }

    @Test
    public void addToHistoryExaclyTheLimit() {
        assertEquals(Arrays.asList(9, 10, 13), statistics.addToHistory(Arrays.asList(9, 10), 13, 3));
    }


    @Test
    public void addToHistoryZeroLimit() {
        assertEquals(Collections.emptyList(), statistics.addToHistory(Arrays.asList(9, 10, 11, 12), 13, 0));
    }
}
