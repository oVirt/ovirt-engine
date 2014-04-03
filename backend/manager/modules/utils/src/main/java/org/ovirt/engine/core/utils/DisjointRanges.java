package org.ovirt.engine.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.math.LongRange;

/**
 * Util to remove overlaps from given ranges. Given ranges are narrowed if needed so that there are no overlaps among
 * all given ranges.
 */
public class DisjointRanges {
    private List<LongRange> disjointRanges = new LinkedList<>();

    /**
     * @param ranges ranges to process and add them (potentially altered) to result list of ranges.
     */
    public void addRanges(List<LongRange> ranges) {
        for (LongRange range : ranges) {
            addRange(range.getMinimumLong(), range.getMaximumLong());
        }
    }

    /**
     * add range to process and add it (potentially altered) to result list of ranges.
     *
     * @param from range left boundary
     * @param to range right boundary
     */
    public void addRange(long from, long to) {
        LongRange addedRange = new LongRange(from, to);
        Iterator<LongRange> it = disjointRanges.iterator();
        while (it.hasNext()) {
            LongRange existingRange = it.next();

            if (existingRange.overlapsRange(addedRange)) {
                it.remove();
                addedRange = new LongRange(Math.min(addedRange.getMinimumLong(), existingRange.getMinimumLong()),
                        Math.max(addedRange.getMaximumLong(), existingRange.getMaximumLong()));
            }
        }

        disjointRanges.add(addedRange);
    }

    /**
     * @return unmodifiable list of ranges, copies of input ranges potentially altered in such way, that there are
     * no overlaps among them.
     */
    public Collection<LongRange> getRanges() {
        return Collections.unmodifiableCollection(disjointRanges);
    }
}
