package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.math.Range;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MacAddressRangeUtils {

    private static final Log log = LogFactory.getLog(MacAddressRangeUtils.class);

    private static final int HEX_RADIX = 16;

    public static final long MAC_ADDRESS_MULTICAST_BIT = 0x010000000000L;
    public static final char BOUNDARIES_DELIMITER = '-';

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
        List<String[]> rangeStringBoundaries = rangeStringToStringBoundaries(ranges);

        return parseRangeStringBoundaries(rangeStringBoundaries);
    }

    public static Collection<LongRange> parseRangeStringBoundaries(List<String[]> rangeStringBoundaries) {
        DisjointRanges disjointRanges = new DisjointRanges();
        for (String[] rangeStringBoundary : rangeStringBoundaries) {
            disjointRanges.addRange(macToLong(rangeStringBoundary[0]), macToLong(rangeStringBoundary[1]));
        }

        return clipMultiCastsFromRanges(disjointRanges.getRanges());
    }

    public static List<String[]> rangeStringToStringBoundaries(String rangeString) {
        if (StringUtils.isEmpty(rangeString)) {
            return Collections.emptyList();
        }

        List<String[]> result = new ArrayList<>();
        String[] rangesArray = rangeString.split("[,]", -1);
        for (int i = 0; i < rangesArray.length; i++) {
            String[] startEndArray = rangesArray[i].split("["+ BOUNDARIES_DELIMITER+"]", -1);

            if (startEndArray.length == 2) {
                result.add(new String[]{ startEndArray[0], startEndArray[1]});
            } else {
                throw new IllegalArgumentException(
                        "Failed to initialize Mac Pool range. Please fix Mac Pool range: rangesArray[i]");
            }
        }

        return result;
    }

    private static Collection<LongRange> clipMultiCastsFromRanges(Collection<LongRange> ranges) {
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
            log.warnFormat(
                    "User supplied range({0}) contains only multicast addresses, so this range is not usable.", range);
            return null;
        }

        final LongRange result = new LongRange(rangeStart, rangeEnd);
        if (trimmingOccurred) {
            log.warnFormat("User supplied range({0}) need to be trimmed to {1}, ", range, result);
        }
        return result;
    }

    public static boolean macIsMulticast(long mac) {
        return (MAC_ADDRESS_MULTICAST_BIT & mac) != 0;
    }

    public static String boundariesToRangeString(long from, long to) {
        return macToString(from) + BOUNDARIES_DELIMITER + macToString(to);
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
        return Long.parseLong(StringUtils.remove(mac, ':'), HEX_RADIX);
    }

}
