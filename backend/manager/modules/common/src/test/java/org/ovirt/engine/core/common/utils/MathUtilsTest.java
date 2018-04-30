package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MathUtilsTest {
    private static long[] args1 = {5*3, 7*3, 3, 0, 10};
    private static long[] args2 = {7*3, 5*3, 5, 10, 0};
    private static long[] gcds = {3, 3, 1, 10, 10};
    private static long[] lcms = {5*7*3, 5*7*3, 3*5, 0, 0};

    @ParameterizedTest
    @MethodSource
    public void checkGcd(long arg1, long arg2, long gcd) {
        assertEquals(gcd, MathUtils.greatestCommonDivisor(arg1, arg2));
    }

    public static Stream<Arguments> checkGcd() {
        return argsWithResult(gcds);
    }

    @ParameterizedTest
    @MethodSource
    public void checkLcm(long arg1, long arg2, long lcm) {
        assertEquals(lcm, MathUtils.leastCommonMultiple(arg1, arg2));
    }

    public static Stream<Arguments> checkLcm() {
        return argsWithResult(lcms);
    }

    public static Stream<Arguments> argsWithResult(long[] result) {
        return IntStream.range(0, args1.length).mapToObj(i -> Arguments.of(args1[i], args2[i], result[i]));
    }
}
