package org.ovirt.engine.core.common.utils;

import java.math.BigDecimal;
import java.util.Collection;

public class ObjectUtils {

    /**
     * Compares if two objects are equal, handling the cases where one of both of them may be null
     * @param obj1
     * @param obj2
     * @return
     */
    public static <T> boolean objectsEqual(T a, T b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static boolean bigDecimalEqual(BigDecimal a, BigDecimal b) {
        return (a == b) || (a != null && b != null && a.compareTo(b) == 0);
    }

    /**
     * Returns if both contains the same elements, regardless of order
     */
    public static boolean haveSameElements(Collection<?> c1, Collection<?> c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null || c2 == null || c1.size() != c2.size()) {
            return false;
        }
        return c1.containsAll(c2) && c2.containsAll(c1);
    }
}
