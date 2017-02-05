package org.ovirt.engine.core.config.entity.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MacAddressPoolRangesValueHelperTest {

    private MacAddressPoolRangesValueHelper validator = new MacAddressPoolRangesValueHelper();
    @Parameterized.Parameter(0)
    public String ranges;
    @Parameterized.Parameter(1)
    public boolean expectedResult;

    @Test
    public void validateRanges() {
        assertEquals(expectedResult, validator.validate(null, ranges).isOk());
    }

    @Parameterized.Parameters
    public static Object[][] ipAddressParams() {
        return new Object[][] {
                { "00:00:00:00:00:00-00:00:00:00:00:FF", true },
                { "00:1A:4A:16:88:FD-00:1A:4A:16:88:FD", true },
                { "AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB", true },
                { "AA:AA:AA:AA:AA:AA-aa:aa:aa:aa:aa:ab", true },
                { "aa:aa:aa:aa:aa:aa-AA:AA:AA:AA:AA:AB", true },
                { "AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB,AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB", true },
                { "AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB,CC:CC:CC:CC:CC:CC-CC:CC:CC:CC:CC:CD", true },
                { "CC:CC:CC:CC:CC:CC-CC:CC:CC:CC:CC:CD,AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB", true },
                { "BB:BB:BB:BB:BB:BB-AA:AA:AA:AA:AA:AA", false },
                { "BB:BB:BB:BB:BB:BB-aa:aa:aa:aa:aa:aa", false },
                { "bb:bb:bb:bb:bb:bb-AA:AA:AA:AA:AA:AA", false },
                { "BB:BB:BB:BB:BB:BA,BB:BB:BB:BB:BB:BB", false },
                { "AA:AA:AA:AA:AA,BB:BB:BB:BB:BB:BB", false },
                { "AA-AA-AA-AA-AA-AA-BB-BB-BB-BB-BB-BB", false },
                { "AA:AA:AA:AA:AA:AA-AA:AA:AA:AA:AA:AB,XA:AA:AA:AA:AA:AA-BB:BB:BB:BB:BB:BB", false },
                { null, false },
                { "", false },
                { " ", false },
        };
    }
}
