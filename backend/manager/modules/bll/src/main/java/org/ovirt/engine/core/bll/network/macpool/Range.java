package org.ovirt.engine.core.bll.network.macpool;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.LongRange;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;

class Range {
    private final LongRange range;
    private final int numberOfMacsInRange;

    /**
     * object counter, which holds number of MACs duplicates.
     */
    private final ObjectCounter<Integer> macDuplicityCount = new ObjectCounter<>(true);
    private int availableMacsCount;

    private BitSet usedMacs;
    private int startingLocationWhenSearchingForUnusedMac = 0;

    public Range(LongRange range) {
        this.range = range;
        long numberOfMacsLong =  (range.getMaximumLong() - range.getMinimumLong()) + 1;
        Validate.isTrue(numberOfMacsLong <= Integer.MAX_VALUE,
                String.format("Range too big; Range shouldn't be bigger than %1$s, but passed one "
                        + "contains %2$s elements.", Integer.MAX_VALUE, numberOfMacsLong));

        numberOfMacsInRange = (int) numberOfMacsLong;

        this.availableMacsCount = numberOfMacsInRange;
        this.usedMacs = new BitSet(numberOfMacsInRange);
    }

    public boolean contains(long mac) {
        return range.containsLong(mac);
    }

    public boolean containsDuplicates() {
        return macDuplicityCount.containsCounts();
    }

    private void checkIfMacIsFromWithinRange(long mac) {
        if (!contains(mac)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param mac mac to add
     *
     * @return if mac was used (it's usage count was increased). I.e. if it was not used, it's used now, or
     * it was used and duplicates are allowed so it's not used one more time.
     */
    public boolean use(long mac, boolean allowDuplicates) {
        checkIfMacIsFromWithinRange(mac);
        int arrayIndex = macToArrayIndex(mac);

        if (!usedMacs.get(arrayIndex)) {
            availableMacsCount--;
            usedMacs.set(arrayIndex, true);
            return true;
        }

        if (allowDuplicates) {
            return macDuplicityCount.increase(arrayIndex);
        } else {
            return false;
        }
    }

    private int macToArrayIndex(long mac) {
        return (int) (mac - range.getMinimumLong());
    }

    public boolean isAllocated(long mac) {
        checkIfMacIsFromWithinRange(mac);
        return usedMacs.get(macToArrayIndex(mac));
    }

    public void freeMac(long mac) {
        checkIfMacIsFromWithinRange(mac);

        int arrayIndex = macToArrayIndex(mac);
        if (!usedMacs.get(arrayIndex)) {
            return;
        }

        final boolean duplicatesExist = macDuplicityCount.count(arrayIndex) != 0;
        if (duplicatesExist) {
            macDuplicityCount.decrease(arrayIndex);
        } else {
            usedMacs.set(arrayIndex, false);
            availableMacsCount++;
        }
    }

    public int getAvailableCount() {
        return availableMacsCount;
    }

    public int getNumberOfMacsInRange() {
        return numberOfMacsInRange;
    }

    public List<Long> allocateMacs(int numberOfMacs, Predicate<String> skipAllocationPredicate) {
        if (numberOfMacs > getAvailableCount()) {
            throw new IllegalStateException("Insufficient amount of free MACs.");
        }

        List<Long> result = new ArrayList<>(numberOfMacs);

        int remainingMacs = numberOfMacs;
        int remainingAttempts = availableMacsCount;
        while (remainingMacs > 0 && remainingAttempts > 0) {
            remainingAttempts -= 1;
            final long mac = findUnusedMac();
            String macAddress = MacAddressRangeUtils.macToString(mac);
            if (skipAllocationPredicate.test(macAddress)) {
                continue;
            }

            // Well duplicates may be allowed, but we're using unallocated mac.
            use(mac, false);
            result.add(mac);
            remainingMacs -= 1;
        }

        return result;
    }

    private long findUnusedMac() {
        int index = usedMacs.nextClearBit(startingLocationWhenSearchingForUnusedMac);
        // below predicate is never true bc largest index value is one less than the size.
        // see https://docs.oracle.com/javase/7/docs/api/java/util/BitSet.html#BitSet(int)
        boolean notFound = index == numberOfMacsInRange;
        if (notFound) {
            index = usedMacs.nextClearBit(0);
        }
        startingLocationWhenSearchingForUnusedMac = (index + 1) % numberOfMacsInRange;

        return range.getMinimumLong() + index;
    }

    boolean overlaps(Range other) {
        return range.overlapsRange(other.range);
    }
}
