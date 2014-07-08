package org.ovirt.engine.core.common.businessentities;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class VmStatisticsTest {

    private VmStatistics statistics = new VmStatistics();

    @Test
    public void addToHistory_addingNull() {
        assertEquals(Arrays.asList(1, 2), statistics.addToHistory(Arrays.asList(1, 2), null, 100));
    }

    @Test
    public void addToHistory_nullBefore() {
        assertEquals(Arrays.asList(12), statistics.addToHistory(null, 12, 100));
    }

    @Test
    public void addToHistory_emptyBefore() {
        assertEquals(Arrays.asList(12), statistics.addToHistory(new ArrayList<Integer>(), 12, 100));
    }

    @Test
    public void addToHistory_oneValueBefore() {
        assertEquals(Arrays.asList(10, 12), statistics.addToHistory(Arrays.asList(10), 12, 100));
    }

    @Test
    public void addToHistory_twoValuesBefore() {
        assertEquals(Arrays.asList(10, 11, 12), statistics.addToHistory(Arrays.asList(10, 11), 12, 100));
    }

    @Test
    public void addToHistory_oneOverLimitValuesBefore() {
        assertEquals(Arrays.asList(11, 12, 13), statistics.addToHistory(Arrays.asList(10, 11, 12), 13, 3));
    }

    @Test
    public void addToHistory_twoOverLimitValuesBefore() {
        assertEquals(Arrays.asList(11, 12, 13), statistics.addToHistory(Arrays.asList(9, 10, 11, 12), 13, 3));
    }

    @Test
    public void addToHistory_oneLimit() {
        assertEquals(Arrays.asList(13), statistics.addToHistory(Arrays.asList(9, 10, 11, 12), 13, 1));
    }

    @Test
    public void addToHistory_exaclyTheLimit() {
        assertEquals(Arrays.asList(9, 10, 13), statistics.addToHistory(Arrays.asList(9, 10), 13, 3));
    }


    @Test
    public void addToHistory_zeroLimit() {
        assertEquals(Collections.emptyList(), statistics.addToHistory(Arrays.asList(9, 10, 11, 12), 13, 0));
    }
}
