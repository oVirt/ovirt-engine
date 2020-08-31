package org.ovirt.engine.core.bll.network.macpool;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.di.Injector;

class MacsStorage {
    private final boolean allowDuplicates;
    private List<Range> ranges = new LinkedList<>();
    private ObjectCounter<Long> customMacs;
    private int startIndexForEmptyRangeSearch = 0;
    private Predicate<String> skipAllocationPredicate;

    public MacsStorage(boolean allowDuplicates) {
        this(allowDuplicates, new MacAddressGlobalUsageTester(allowDuplicates));
    }

    MacsStorage(boolean allowDuplicates, Predicate<String> skipAllocationPredicate) {
        this.allowDuplicates = allowDuplicates;
        customMacs = new ObjectCounter<>(this.allowDuplicates);
        this.skipAllocationPredicate = skipAllocationPredicate;
    }

    void setSkipAllocationPredicate(Predicate<String> skipAllocationPredicate) {
        this.skipAllocationPredicate = skipAllocationPredicate;
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

        final List<Long> result = new LinkedList<>();
        int remainingMacs = allocateAvailableMacs(result, numberOfMacs, this.skipAllocationPredicate);
        if (remainingMacs > 0) {
            auditAllocatingMacsInUse(remainingMacs);
            allocateAvailableMacs(result, remainingMacs, any -> false);
        }

        return result;
    }

    private int allocateAvailableMacs(List<Long> allocatedMacs, int numberOfMacs, Predicate<String> skipAllocationPredicate) {
        int remainingRanges = ranges.size();
        int reminaingMacs = numberOfMacs;
        while (reminaingMacs > 0 && remainingRanges > 0) {
            final Range rangeWithAvailableMac = getRangeWithAvailableMac();
            Validate.notNull(rangeWithAvailableMac);

            final int availableMacsCount = rangeWithAvailableMac.getAvailableCount();
            int allocatingMacsCount = Math.min(availableMacsCount, reminaingMacs);

            final List<Long> allocatedMacsForRange = rangeWithAvailableMac.allocateMacs(
                allocatingMacsCount, skipAllocationPredicate
            );

            if (allocatedMacsForRange.size() > 0) {
                reminaingMacs -= allocatedMacsForRange.size();
                allocatedMacs.addAll(allocatedMacsForRange);
            }
            remainingRanges -= 1;
        }
        return reminaingMacs;
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

    public int getTotalNumberOfMacs() {
        int count = 0;
        for (Range range : ranges) {
            count += range.getNumberOfMacsInRange();
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

    boolean containsDuplicates() {
        return ranges.stream().anyMatch(range -> range.containsDuplicates()) || customMacs.containsDuplicates();
    }

    boolean overlaps(Range range) {
        return ranges.stream().anyMatch(r -> r.overlaps(range));
    }

    Collection<Range> getRanges() {
        return ranges;
    }

    boolean overlaps(MacsStorage other) {
        return other.getRanges().stream().anyMatch(this::overlaps);
    }

    void auditAllocatingMacsInUse(int countMacsInUse) {
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("NumberOfMacs", Integer.toString(countMacsInUse));
        Injector.get(AuditLogDirector.class).log(logable, AuditLogType.MAC_ADDRESS_IN_USE_ALLOCATED);
    }
}
