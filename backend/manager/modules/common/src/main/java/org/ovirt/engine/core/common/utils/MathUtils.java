package org.ovirt.engine.core.common.utils;

public class MathUtils {

    public static long greatestCommonDivisor(long a, long b) {
        while (b != 0) {
            long c = b;
            b = a % b;
            a = c;
        }

        return a;
    }

    public static long leastCommonMultiple(long a, long b) {
        return Math.abs(a * b) / greatestCommonDivisor(a, b);
    }
}
