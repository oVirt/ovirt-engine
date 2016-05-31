package org.ovirt.engine.core.bll.network.macpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;

class MacsStorage {
    private final boolean allowDuplicates;
    private List<Range> ranges = new LinkedList<>();
    private ObjectCounter<Long> customMacs;
    private int startIndexForEmptyRangeSearch = 0;

    public MacsStorage(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        customMacs = new ObjectCounter<>(this.allowDuplicates);
    }

    public Range addRange(long rangeStart, long rangeEnd) {
        return addRange(new Range(rangeStart, rangeEnd));
    }

    Range addRange(Range range) {
        ranges.add(range);
        return range;
    }

    public boolean useMac(long mac) {
        return useMac(mac, allowDuplicates);
    }

    private boolean useMac(long mac, boolean allowDuplicates) {
        Range range = findIncludingRange(mac);
        if (range == null) {
            return customMacs.increase(mac, allowDuplicates);
        } else {
            return range.use(mac, allowDuplicates);
        }
    }

    public void useMacNoDuplicityCheck(long mac) {
        useMac(mac, true);
    }

    public boolean isMacInUse(long mac) {
        Range range = findIncludingRange(mac);
        return range == null ? customMacs.contains(mac) : range.isAllocated(mac);
    }

    public void freeMac(long mac) {
        Range range = findIncludingRange(mac);
        if (range == null) {
            customMacs.decrease(mac);
        } else {
            range.freeMac(mac);
        }
    }

    public boolean availableMacExist() {
        return getRangeWithAvailableMac() != null;
    }

    public List<Long> allocateAvailableMacs(int numberOfMacs) {
        if (getAvailableMacsCount() < numberOfMacs) {
            throw new EngineException(EngineError.MAC_POOL_NO_MACS_LEFT);
        }

        final List<Long> result = new ArrayList<>(numberOfMacs);
        int remainingMacs = numberOfMacs;
        while (remainingMacs > 0) {
            final Range rangeWithAvailableMac = getRangeWithAvailableMac();
            Validate.notNull(rangeWithAvailableMac);

            final List<Long> allocatedMacs = rangeWithAvailableMac.allocateMacs(remainingMacs);

            remainingMacs -= allocatedMacs.size();
            result.addAll(allocatedMacs);
        }

        return result;
    }

    Range getRangeWithAvailableMac() {
        int numberOfRanges = ranges.size();
        Range range = findRangeWithAvailableMac(startIndexForEmptyRangeSearch, numberOfRanges, numberOfRanges);
        if (range != null) {
            return range;
        }

        return findRangeWithAvailableMac(0, startIndexForEmptyRangeSearch, numberOfRanges);
    }

    private Range findRangeWithAvailableMac(int startIndex,
            int endIndex, int numberOfRanges) {
        for(int i = startIndex; i < endIndex; i++) {
            Range range = ranges.get(i);
            boolean hasAvailableMacs = range.getAvailableCount() > 0;
            if (hasAvailableMacs) {
                startIndexForEmptyRangeSearch = (i + 1) % numberOfRanges;
                return range;
            }
        }

        return null;
    }

    public int getAvailableMacsCount() {
        int count = 0;
        for (Range range : ranges) {
            count += range.getAvailableCount();
        }
        return count;
    }

    private Range findIncludingRange(long mac) {
        for (Range range : ranges) {
            if (range.contains(mac)) {
                return range;
            }
        }
        return null;
    }

    boolean isMacInRange(Long mac) {
        return findIncludingRange(mac) != null;
    }
}
