package org.ovirt.engine.core.common.utils;

import java.math.BigDecimal;

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

}
