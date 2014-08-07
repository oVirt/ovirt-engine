package org.ovirt.engine.core.bll.network.macpoolmanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;

class MacsStorage {
    private final boolean allowDuplicates;
    private List<Range> ranges = new LinkedList<>();
    private ObjectCounter<Long> customMacs;

    public MacsStorage(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        customMacs = new ObjectCounter<>(this.allowDuplicates);
    }

    public void addRange(long rangeStart, long rangeEnd) {
        ranges.add(new Range(rangeStart, rangeEnd));
    }

    public boolean useMac(long mac) {
        return useMac(mac, allowDuplicates);
    }

    private boolean useMac(long mac, boolean allowDuplicates) {
        Range range = findIncludingRange(mac);
        if (range == null) {
            return customMacs.add(mac, allowDuplicates);
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
            customMacs.remove(mac);
        } else {
            range.freeMac(mac);
        }
    }

    public boolean availableMacExist() {
        return getRangeWithAvailableMac() != null;
    }

    public List<Long> allocateAvailableMacs(int numberOfMacs) {
        if (getAvailableMacsCount() < numberOfMacs) {
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_NO_MACS_LEFT);
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

    private Range getRangeWithAvailableMac() {
        for (Range range : ranges) {
            if (range.getAvailableCount() > 0) {
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
}
