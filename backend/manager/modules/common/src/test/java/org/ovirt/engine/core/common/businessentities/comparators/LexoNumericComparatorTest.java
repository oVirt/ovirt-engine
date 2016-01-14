package org.ovirt.engine.core.common.businessentities.comparators;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class LexoNumericComparatorTest {

    private LexoNumericComparator comparator;

    private String left;
    private String right;
    private int expectedResult;

    public LexoNumericComparatorTest(boolean caseSensitive, String left, String right, int expectedResult) {
        comparator = new LexoNumericComparator(caseSensitive);
        this.left = left;
        this.right = right;
        this.expectedResult = expectedResult;
    }

    private void verifyResult(String left, String right, int expectedResult) {
        assertEquals(String.format("Expected %1$s to be %3$s %2$s, but it wasn't.",
                left,
                right,
                expectedResult == -1 ? "less than" : expectedResult == 1 ? "greater than" : "equal to"),
                expectedResult,
                comparator.compare(left, right));
    }

    @Test
    public void runTest() {
        verifyResult(left, right, expectedResult);
        verifyResult(right, left, -expectedResult);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> comparisonParameters() {
        return Arrays.asList(new Object[][] {
                { false, null, null, 0 },
                { false, null, "", -1 },
                { false, "", "", 0 },
                { false, "", "123", -1 },
                { false, "123", "123", 0 },
                { false, "123", "456", -1 },
                { false, "12", "123", -1 },
                { false, "012", "12", -1 },
                { false, "2", "10", -1 },
                { false, "123abc", "123abc", 0 },
                { true, "123abc", "123abc", 0 },
                { false, "123Abc", "123abc", -1 },
                { true, "123Abc", "123abc", -1 },
                { false, "123abc", "456abc", -1 },
                { false, "12abc", "123abc", -1 },
                { false, "012abc", "12abc", -1 },
                { false, "2abc", "10abc", -1 },
                { false, "123", "abc", -1 },
                { false, "abc", "abc", 0 },
                { true, "abc", "abc", 0 },
                { false, "Abc", "abc", -1 },
                { true, "Abc", "abc", -1 },
                { false, "abc", "def", -1 },
                { false, "ab", "abc", -1 },
                { false, "123abc", "123def", -1 },
                { false, "123ab", "123abc", -1 },
                { false, "abc123", "abc123", 0 },
                { true, "abc123", "abc123", 0 },
                { false, "Abc123", "abc123", -1 },
                { true, "Abc123", "abc123", -1 },
                { false, "Abc234", "abc123", 1 },
                { true, "Abc234", "abc123", -1 },
                { false, "Abc1234", "abc123", 1 },
                { true, "Abc1234", "abc123", -1 },
                { false, "abc123", "def123", -1 },
                { false, "ab123", "abc123", -1 },
                { false, "abc123", "abc456", -1 },
                { false, "abc12", "abc123", -1 },
                { false, "abc012", "abc12", -1 },
                { false, "abc2", "abc10", -1 },
                { false, "abc123def", "abc123def", 0 },
                { true, "abc123def", "abc123def", 0 },
                { false, "Abc123def", "abc123def", -1 },
                { true, "Abc123def", "abc123def", -1 },
                { false, "Abc234def", "abc123def", 1 },
                { true, "Abc234def", "abc123def", -1 },
                { false, "Abc1234def", "abc123def", 1 },
                { true, "Abc1234def", "abc123def", -1 },
                { false, "abc123def", "abc123ghi", -1 },
                { false, "abc123de", "abc123def", -1 },
                { false, "abc123456789123de", "abc123456789123de", 0 },
                { true, "abc123456789123de", "abc123456789123de", 0 },
                { false, "Abc123456789123de", "abc123456789123de", -1 },
                { true, "Abc123456789123de", "abc123456789123de", -1 },
                { false, "Abc123456789234de", "abc123456789123de", 1 },
                { true, "Abc123456789234de", "abc123456789123de", -1 },
                { false, "Abc1234567891234de", "abc123456789123de", 1 },
                { true, "Abc1234567891234de", "abc123456789123de", -1 },
                { false, "abc123456789123de", "abc123456789123fg", -1 },
                { false, "abc123456789123de", "abc123456789123def", -1 }
        });
    }
}
