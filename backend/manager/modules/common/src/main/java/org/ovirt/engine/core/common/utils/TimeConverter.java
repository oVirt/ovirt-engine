package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeConverter {

    private static List<TimeUnit> orderedTimeUnits;

    static {
        orderedTimeUnits = new ArrayList<>();
        for (TimeUnit timeUnit : TimeUnit.values()) {
            orderedTimeUnits.add(timeUnit);
        }
        Collections.sort(orderedTimeUnits, Collections.reverseOrder(new Comparator<TimeUnit>() {
            @Override
            public int compare(TimeUnit unit1, TimeUnit unit2) {
                if (TimeUnit.MICROSECONDS.convert(1L, unit1) < TimeUnit.MICROSECONDS.convert(1L, unit2)) {
                    return -1;
                }
                return 1;
            }
        }));
    }

    /*
     * This method converts an input time interval (interval) in an input unit(fromUnit) to a desired output
     * unit(toUnit). It is basically a wrapper around the java.util.concurrent.TimeUnit 's convert function This is
     * method is written just for ease of use as the java.util.concurrent.TimeUnit 's convert function usage is of the
     * form TimeUnit.Minutes.convert(60L, TimeUnit.Seconds) to convert 60 seconds to a minuite
     */

    public static long convert(long interval, TimeUnit fromUnit, TimeUnit toUnit) {
        return TimeUnit.valueOf(toUnit.name()).convert(interval, fromUnit);
    }

    /*
     * This method is introduced for the purpose of making it easy to express the input interval of time in an input
     * unit into its highest possible unit such that the integral value of the converted interval is of non-zero
     * magnitude. ex : 60 * 1000 * 1000 microseconds = 1 min
     */

    public static Pair<Long, TimeUnit> autoConvert(long interval, TimeUnit fromUnit) {
        Pair<Long, TimeUnit> result = new Pair<>();
        for (TimeUnit timeUnit : orderedTimeUnits) {
            long tInterval = interval;
            interval = convert(interval, fromUnit, timeUnit);
            if (interval >= 1) {
                result.setFirst(interval);
                result.setSecond(timeUnit);
                break;
            }
            interval = tInterval;
        }
        return result;
    }
}
