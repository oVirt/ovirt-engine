package org.ovirt.engine.core.bll.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RangeTest {

    private static final int MAC_FROM_RANGE = 15;
    private static final int MAC_OUTSIDE_OF_RANGE = 25;
    private static final int RANGE_FROM = 10;
    private static final int RANGE_TO = 19;
    private static final int NUMBER_OF_MACS = 10;
    private Range rangeOf10Macs;

    @Before
    public void before() {
        rangeOf10Macs = new Range(RANGE_FROM, RANGE_TO);
    }

    @Test
    public void testMacIsContainedInRange() throws Exception {
        assertThat(rangeOf10Macs.contains(MAC_FROM_RANGE), is(true));
    }
    @Test
    public void testMacIsNotContainedInRange() throws Exception {
        assertThat(rangeOf10Macs.contains(MAC_OUTSIDE_OF_RANGE), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailWhenUsingMacOutsideOfRange() throws Exception {
        rangeOf10Macs.use(MAC_OUTSIDE_OF_RANGE, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailWhenAskingForMacOutsideOfRange() throws Exception {
        rangeOf10Macs.isAllocated(MAC_OUTSIDE_OF_RANGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailWhenReturningMacOutsideOfRange() throws Exception {
        rangeOf10Macs.freeMac(MAC_OUTSIDE_OF_RANGE);
    }

    @Test
    public void testAllMacsAreAvailableAfterCreation() {
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS));
    }

    @Test
    public void testAssigningMacWithDisallowedDuplicates() throws Exception {
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, false), is(true));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, false), is(false));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
    }

    @Test
    public void testAssigningMacWithAllowedDuplicates() throws Exception {
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
    }

    @Test
    public void testFreeMac() throws Exception {
        final List<Long> allocatedMacs = rangeOf10Macs.allocateMacs(NUMBER_OF_MACS);
        assertThat(allocatedMacs.size(), is(NUMBER_OF_MACS));
        assertThat(rangeOf10Macs.getAvailableCount(), is(0));

        for(int i = 1; i <= NUMBER_OF_MACS; i++) {
            rangeOf10Macs.freeMac(allocatedMacs.remove(0));
            assertThat(rangeOf10Macs.getAvailableCount(), is(i));
        }
    }

    @Test
    public void testFreeMacDuplicityAllowed() throws Exception {
        // Allocate one mac twice.
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));

        // Check decreasing of duplicity usage.
        rangeOf10Macs.freeMac(MAC_FROM_RANGE);
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
        rangeOf10Macs.freeMac(MAC_FROM_RANGE);
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(false));
    }

    @Test
    public void testAllocateMac() throws Exception {
        assertThat(rangeOf10Macs.allocateMacs(5).size(), is(5));
        assertThat(rangeOf10Macs.getAvailableCount(), is(5));
        assertThat(rangeOf10Macs.allocateMacs(5).size(), is(5));
        assertThat(rangeOf10Macs.getAvailableCount(), is(0));
    }

    @Test(expected = IllegalStateException.class)
    public void testAllocateMacNoEnoughMacs() throws Exception {
        rangeOf10Macs.allocateMacs(NUMBER_OF_MACS + 1);
    }
}
