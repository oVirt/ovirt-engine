package org.ovirt.engine.core.bll.network.macpool;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang.Validate;

class Range {
    private final long rangeStart;
    private final long rangeEnd;
    private final int numberOfMacsInRange;

    /**
     * object counter, which holds number of MACs duplicates.
     */
    private final ObjectCounter<Integer> macDuplicityCount = new ObjectCounter<>(true);
    private int availableMacsCount;

    private BitSet usedMacs;
    private int startingLocationWhenSearchingForUnusedMac = 0;

    public Range(long rangeStart, long rangeEnd) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;

        long numberOfMacsLong =  (rangeEnd - rangeStart) + 1;
        Validate.isTrue(numberOfMacsLong <= Integer.MAX_VALUE,
                String.format("Range too big; Range shouldn't be bigger than %1$s, but passed one "
                        + "contains %2$s elements.", Integer.MAX_VALUE, numberOfMacsLong));

        numberOfMacsInRange = (int) numberOfMacsLong;

        this.availableMacsCount = numberOfMacsInRange;
        this.usedMacs = new BitSet(numberOfMacsInRange);
    }

    public boolean contains(long mac) {
        return rangeStart <= mac && rangeEnd >= mac;
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
        return (int) (mac - rangeStart);
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

    public List<Long> allocateMacs(int numberOfMacs) {
        if (numberOfMacs > getAvailableCount()) {
            throw new IllegalStateException("Insufficient amount of free MACs.");
        }

        List<Long> result = new ArrayList<>(numberOfMacs);

        for (int count = 0; count < numberOfMacs; count++) {
            final long mac = findUnusedMac();

            // Well duplicates may be allowed, but we're using unallocated mac.
            use(mac, false);
            result.add(mac);
        }

        return result;
    }

    private long findUnusedMac() {
        int index = usedMacs.nextClearBit(startingLocationWhenSearchingForUnusedMac);
        boolean notFound = index == numberOfMacsInRange;
        if (notFound) {
            index = usedMacs.nextClearBit(0);
        }
        startingLocationWhenSearchingForUnusedMac = (index + 1) % numberOfMacsInRange;

        return rangeStart + index;
    }

}
