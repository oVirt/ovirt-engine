package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MathUtilsTest {

    @Parameterized.Parameter(0)
    public long arg1;
    @Parameterized.Parameter(1)
    public long arg2;
    @Parameterized.Parameter(2)
    public long gcd;
    @Parameterized.Parameter(3)
    public long lcm;

    @Test
    public void checkGcd() {
        assertEquals(gcd, MathUtils.greatestCommonDivisor(arg1, arg2));
    }

    @Test
    public void checkLcm() {
        assertEquals(lcm, MathUtils.leastCommonMultiple(arg1, arg2));
    }

    @Parameterized.Parameters
    public static Object[][] params() {
        return new Object[][] {
                {5*3, 7*3, 3, 5*7*3},
                {7*3, 5*3, 3, 5*7*3},

                {3, 5, 1, 3*5},

                {0, 10, 10, 0},
                {10, 0, 10, 0}
        };
    }
}
