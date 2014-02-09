package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class MacAddressRangeUtils {

    private static final long MAC_ADDRESS_MULTICAST_BIT = 0x010000000000L;
    private static final int HEX_RADIX = 16;

    public static List<String> initRange(String start, String end, int size) {
        return innerInitRange(start, end, size);
    }

    private static List<String> innerInitRange(String start, String end, int stopAfter) {
        String parsedRangeStart = StringUtils.remove(start, ':');
        String parsedRangeEnd = StringUtils.remove(end, ':');
        if (parsedRangeEnd == null || parsedRangeStart == null) {
            return Collections.emptyList();
        }

        long startNum = Long.parseLong(parsedRangeStart, HEX_RADIX);
        long endNum = Long.parseLong(parsedRangeEnd, HEX_RADIX);
        if (startNum > endNum) {
            return Collections.emptyList();
        }

        List<String> macAddresses = new ArrayList<String>();
        for (long i = startNum; i <= endNum; i++) {
            if ((MAC_ADDRESS_MULTICAST_BIT & i) != 0) {
                continue;
            }

            String value = String.format("%012x", i);
            macAddresses.add(StringUtils.join(value.split("(?<=\\G..)"), ':'));

            if (stopAfter-- <= 0) {
                return macAddresses;
            }
        }

        return macAddresses;
    }

    public static boolean isRangeValid(String start, String end) {
        return !innerInitRange(start, end, 1).isEmpty();
    }
}
