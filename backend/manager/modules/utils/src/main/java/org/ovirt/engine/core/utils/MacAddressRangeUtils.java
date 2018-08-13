package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.math.Range;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacAddressRangeUtils {

    private static final Logger log = LoggerFactory.getLogger(MacAddressRangeUtils.class);

    private static final int HEX_RADIX = 16;

    public static final long MAC_ADDRESS_MULTICAST_BIT = 0x010000000000L;

    private MacAddressRangeUtils() {
    }

    public static List<String> macAddressesToStrings(List<Long> macAddresses) {
        final List<String> result = new ArrayList<>(macAddresses.size());

        for (Long macAddress : macAddresses) {
            result.add(macToString(macAddress));
        }

        return result;
    }

    public static Collection<LongRange> parseRangeString(String ranges) {
        if (StringUtils.isEmpty(ranges)) {
            return Collections.emptyList();
        }

        String[] rangesArray = ranges.split("[,]", -1);
        DisjointRanges disjointRanges = new DisjointRanges();

        for (int i = 0; i < rangesArray.length; i++) {
            String[] startEndArray = rangesArray[i].split("[-]", -1);

            if (startEndArray.length == 2) {
                disjointRanges.addRange(macToLong(startEndArray[0]), macToLong(startEndArray[1]));
            } else {
                throw new IllegalArgumentException(
                        "Failed to initialize Mac Pool range. Please fix Mac Pool range: rangesArray[i]");
            }
        }

        return clipMultiCastsFromRanges(disjointRanges.getRanges());
    }

    public static Collection<LongRange> clipMultiCastsFromRanges(Collection<LongRange> ranges) {
        final Collection<LongRange> result = new ArrayList<>();
        for (LongRange range : ranges) {
            final LongRange clippedRange = clipRange(range);
            if (clippedRange != null) {
                result.add(clippedRange);
            }
        }
        return result;
    }

    public static LongRange clipRange(Range range) {
        long rangeEnd = range.getMaximumLong();
        long rangeStart = range.getMinimumLong();

        boolean trimmingOccurred = false;
        if (MacAddressRangeUtils.macIsMulticast(rangeStart)) {
            rangeStart = (rangeStart | 0x00FFFFFFFFFFL) + 1;
            trimmingOccurred = true;
        }

        final long trimmedRangeEnd = Math.min(rangeStart + Integer.MAX_VALUE - 1, rangeEnd);
        if (rangeEnd != trimmedRangeEnd) {
            rangeEnd = trimmedRangeEnd;
            trimmingOccurred = true;
        }

        if (MacAddressRangeUtils.macIsMulticast(rangeEnd)) {
            rangeEnd = (rangeEnd & 0xFF0000000000L) - 1;
            trimmingOccurred = true;
        }

        if (rangeStart > rangeEnd) {
            log.warn(
                    "User supplied range({}) contains only multicast addresses, so this range is not usable.", range);
            return null;
        }

        final LongRange result = new LongRange(rangeStart, rangeEnd);
        if (trimmingOccurred) {
            log.warn("User supplied range({}) need to be trimmed to {}.", range, result);
        }
        return result;
    }

    public static boolean macIsMulticast(long mac) {
        return (MAC_ADDRESS_MULTICAST_BIT & mac) != 0;
    }

    public static String macToString(long macAddress) {
        String value = String.format("%012x", macAddress);
        char[] chars = value.toCharArray();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(chars[0]).append(chars[1]);
        for (int pos = 2; pos < value.length(); pos += 2) {
            stringBuilder.append(":")
                    .append(chars[pos])
                    .append(chars[pos + 1]);
        }

        return stringBuilder.toString();
    }

    public static long macToLong(String mac) {
        log.debug("Processing MAC address: {}.", mac);
        return Long.parseLong(StringUtils.remove(mac, ':'), HEX_RADIX);
    }

    public static boolean isRangeValid(String start, String end) {
        long startNum = macToLong(start);
        long endNum = macToLong(end);

        if (startNum > endNum) {
            return false;
        }

        Collection<LongRange> ranges = parseRangeString(start + "-" + end);

        for (LongRange range : ranges) {
            if (range.getMaximumLong() - range.getMinimumLong() < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * convert mac pool ranges to long ranges for bll usage with union of any two overlapping ranges into one range
     */
    public static  Collection<LongRange> macPoolToRanges(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        final DisjointRanges disjointRanges = new DisjointRanges();
        for (MacRange macRange : macPool.getRanges()) {
            disjointRanges.addRange(macToLong(macRange.getMacFrom()),
                    macToLong(macRange.getMacTo()));
        }
        return clipMultiCastsFromRanges(disjointRanges.getRanges());
    }

    /**
     * convert mac pool ranges to long ranges for bll usage without union of overlapping ranges
     */
    public static  Collection<LongRange> toRanges(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        final List<LongRange> ranges = new ArrayList<>(macPool.getRanges().size());
        for (MacRange macRange : macPool.getRanges()) {
            ranges.add(new LongRange(macToLong(macRange.getMacFrom()), macToLong(macRange.getMacTo())));
        }
        return clipMultiCastsFromRanges(ranges);
    }

    /**
     * @return ranges in this mac pool which overlap each other
     */
    public static Collection<LongRange> filterOverlappingRanges(org.ovirt.engine.core.common.businessentities.MacPool macPool) {
        Collection<LongRange> longRanges = toRanges(macPool);
        return filterOverlappingRangeSets(longRanges, longRanges);
    }

    private static Collection<LongRange> filterOverlappingRangeSets(Collection<LongRange> rangeSet1, Collection<LongRange> rangeSet2) {
        return rangeSet1.stream()
                .filter(range1 -> rangeSet2.stream().anyMatch(range2 -> range1 != range2 && range2.overlapsRange(range1)))
                .collect(Collectors.toList());
    }

    public static Set<MacPool> poolsOverlappedByOtherPool(Collection<MacPool> pools, MacPool otherPool) {
        Set<MacPool> overlappingPools = new HashSet<>();
        pools.forEach(pool -> {
            if (!pool.getId().equals(otherPool.getId()) &&
                !filterOverlappingRangeSets(toRanges(pool), toRanges(otherPool)).isEmpty()) {
                overlappingPools.add(pool);
            }
        });
        return overlappingPools;
    }

}
