package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.ovirt.engine.core.utils.MacAddressRangeUtils.macToLong;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.math.LongRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;

public class RangeTest {

    private static final int MAC_FROM_RANGE = 15;
    private static final int MAC_OUTSIDE_OF_RANGE = 25;
    private static final long RANGE_FROM = 10;
    private static final long RANGE_TO = 19;
    private static final int NUMBER_OF_MACS = 10;
    private Range rangeOf10Macs;

    @BeforeEach
    public void before() {
        rangeOf10Macs = new Range(new LongRange(RANGE_FROM, RANGE_TO));
    }

    @Test
    public void testMacIsContainedInRange() {
        assertThat(rangeOf10Macs.contains(MAC_FROM_RANGE), is(true));
    }
    @Test
    public void testMacIsNotContainedInRange() {
        assertThat(rangeOf10Macs.contains(MAC_OUTSIDE_OF_RANGE), is(false));
    }

    @Test
    public void testFailWhenUsingMacOutsideOfRange() {
        assertThrows(IllegalArgumentException.class, () -> rangeOf10Macs.use(MAC_OUTSIDE_OF_RANGE, false));
    }

    @Test
    public void testFailWhenAskingForMacOutsideOfRange() {
        assertThrows(IllegalArgumentException.class, () -> rangeOf10Macs.isAllocated(MAC_OUTSIDE_OF_RANGE));
    }

    @Test
    public void testFailWhenReturningMacOutsideOfRange() {
        assertThrows(IllegalArgumentException.class, () -> rangeOf10Macs.freeMac(MAC_OUTSIDE_OF_RANGE));
    }

    @Test
    public void testAllMacsAreAvailableAfterCreation() {
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS));
    }

    @Test
    public void testAssigningMacWithDisallowedDuplicates() {
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, false), is(true));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, false), is(false));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
        assertThat(rangeOf10Macs.containsDuplicates(), is(false));
    }

    @Test
    public void testAssigningMacWithAllowedDuplicates() {
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.containsDuplicates(), is(false));
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.getAvailableCount(), is(NUMBER_OF_MACS - 1));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
        assertThat(rangeOf10Macs.containsDuplicates(), is(true));
    }

    @Test
    public void testFreeMac() {
        final List<Long> allocatedMacs = rangeOf10Macs.allocateMacs(NUMBER_OF_MACS, skip -> false);
        assertThat(allocatedMacs.size(), is(NUMBER_OF_MACS));
        assertThat(rangeOf10Macs.getAvailableCount(), is(0));
        assertThat(rangeOf10Macs.containsDuplicates(), is(false));

        for(int i = 1; i <= NUMBER_OF_MACS; i++) {
            rangeOf10Macs.freeMac(allocatedMacs.remove(0));
            assertThat(rangeOf10Macs.getAvailableCount(), is(i));
            assertThat(rangeOf10Macs.containsDuplicates(), is(false));
        }
    }

    @Test
    public void testFreeMacDuplicityAllowed() {
        // Allocate one mac twice.
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
        assertThat(rangeOf10Macs.containsDuplicates(), is(false));
        assertThat(rangeOf10Macs.use(MAC_FROM_RANGE, true), is(true));
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
        assertThat(rangeOf10Macs.containsDuplicates(), is(true));

        // Check decreasing of duplicity usage.
        rangeOf10Macs.freeMac(MAC_FROM_RANGE);
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(true));
        assertThat(rangeOf10Macs.containsDuplicates(), is(false));
        rangeOf10Macs.freeMac(MAC_FROM_RANGE);
        assertThat(rangeOf10Macs.isAllocated(MAC_FROM_RANGE), is(false));
        assertThat(rangeOf10Macs.containsDuplicates(), is(false));
    }

    @Test
    public void testAllocateMac() {
        assertThat(rangeOf10Macs.allocateMacs(5, skip -> false).size(), is(5));
        assertThat(rangeOf10Macs.getAvailableCount(), is(5));
        assertThat(rangeOf10Macs.allocateMacs(5, skip -> false).size(), is(5));
        assertThat(rangeOf10Macs.getAvailableCount(), is(0));
        assertThat(rangeOf10Macs.containsDuplicates(), is(false));
    }

    @Test
    public void testAllocateMacNoEnoughMacs() {
        assertThrows(IllegalStateException.class, () -> rangeOf10Macs.allocateMacs(NUMBER_OF_MACS + 1, skip -> false));
    }

    @Test
    public void testRangeStartAndRangeStopAreInclusive() {
        assertThat(new Range(new LongRange(MAC_FROM_RANGE, MAC_FROM_RANGE)).getAvailableCount(), is(1));
    }

    @Test
    public void testRangeCanContainOnlyIntSizeNumberOfElements() {
        LongRange longRange = MacAddressRangeUtils.clipRange(new LongRange(0, Long.MAX_VALUE));
        Range range = new Range(new LongRange(longRange.getMinimumLong(), longRange.getMaximumLong()));
        assertThat(range.getAvailableCount(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testTooBigRange() {
        assertThrows(IllegalArgumentException.class, () -> new Range(new LongRange(0, Integer.MAX_VALUE)));
    }

    @Test
    public void testMaxSizeRange() {
        new Range(new LongRange(0, Integer.MAX_VALUE - 1));
    }


    /**
     * test that MACs arent returned in leftmost-available order. Instead, we're returning macs from left to right,
     * noting last scan position. That means that if we obtain and return mac from/to pool, we'll obtain this MAC again
     * only after all other free macs were used.
     */
    @Test
    public void testOrderOrAcquiredMACs() {
        Range range = new Range(new LongRange(0, 5));
        List<Integer> usedMacs = Arrays.asList(0, 2, 4);
        for(int i = 0; i < 5; i++) {
            boolean usedMac = usedMacs.contains(i);
            if (usedMac) {
                boolean allowDuplicates = false;
                range.use(i, allowDuplicates);
                assertThat(range.containsDuplicates(), is(false));
            }
        }

        for (Integer expectedUnallocatedMac : Arrays.asList(1, 3, 5, 1, 3, 5, 1)) {
            allocateAndFreeMacAndExpectGivenMac(range, expectedUnallocatedMac);
        }
    }

    @ParameterizedTest
    @MethodSource
    void testAllocateMacsSomeInUse(int macsToAllocate, int macsInUse, int expectedAllocated) {
        Range range = new Range(new LongRange(RANGE_FROM, RANGE_TO));
        List<Long> allocated = range.allocateMacs(macsToAllocate,
            allocatedMac -> macToLong(allocatedMac) < RANGE_FROM + macsInUse
        );
        assertEquals(expectedAllocated, allocated.size());
        assertEquals(NUMBER_OF_MACS - expectedAllocated, range.getAvailableCount());
    }

    static Stream<Object[]> testAllocateMacsSomeInUse() {
        return Stream.of(
            // macsToAllocate, macsInUse, expectedAllocated

            // fully  allocate
            new Object[] {0, NUMBER_OF_MACS, 0},
            new Object[] {1, NUMBER_OF_MACS, 0},
            new Object[] {NUMBER_OF_MACS, NUMBER_OF_MACS, 0},

            // fully allocate - nothing in use elsewhere
            new Object[] {0, 0, 0},
            new Object[] {1, 0, 1},
            new Object[] {NUMBER_OF_MACS, 0, NUMBER_OF_MACS},

            // fully allocate - requested allocation small enough
            new Object[] {1, 1, 1},
            new Object[] {NUMBER_OF_MACS - 1, 1, NUMBER_OF_MACS - 1},
            new Object[] {1, NUMBER_OF_MACS - 1, 1},

            // partial allocate
            new Object[] {NUMBER_OF_MACS, 1, NUMBER_OF_MACS - 1},
            new Object[] {NUMBER_OF_MACS, NUMBER_OF_MACS / 2, NUMBER_OF_MACS / 2},

            // cannot allocate
            new Object[] {0, NUMBER_OF_MACS, 0},
            new Object[] {1, NUMBER_OF_MACS, 0},
            new Object[] {NUMBER_OF_MACS, NUMBER_OF_MACS, 0}
        );
    }

    /***
     *
     * method obtains mac from pool, assert expectation, and return it back.
     *
     * @param range range of macs
     * @param expectedMac mac, which we expect to be returned from {@code range.allocateMacs(1)}
     */
    private void allocateAndFreeMacAndExpectGivenMac(Range range, long expectedMac) {
        Long mac = range.allocateMacs(1, skip -> false).get(0);
        assertThat(mac, is(expectedMac));
        range.freeMac(mac);
    }
}
