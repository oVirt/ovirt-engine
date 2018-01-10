package org.ovirt.engine.core.common.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Function;

public class ObjectUtils {

    public static boolean bigDecimalEqual(BigDecimal a, BigDecimal b) {
        return (a == b) || (a != null && b != null && a.compareTo(b) == 0);
    }

    /**
     * Returns if both contains the same elements, regardless of order and duplicates
     */
    public static boolean haveSameElements(Collection<?> c1, Collection<?> c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null || c2 == null || c1.size() != c2.size()) {
            return false;
        }
        return c1.containsAll(c2);
    }

    /**
     * @return min(a, b) if both a and b are non-null, `null` otherwise
     */
    public static Integer minIfExists(Integer a, Integer b) {
        return a != null && b != null ? Math.min(a, b) : null;
    }

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static <T, U> U mapNullable(T value, Function<T, U> mapper) {
        if (value == null) {
            return null;
        }
        return mapper.apply(value);
    }
}
