package org.ovirt.engine.core.common.businessentities.comparators;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class LexoNumericComparatorTest {

    private LexoNumericComparator comparator = new LexoNumericComparator();

    private String left;
    private String right;
    private int expectedResult;

    public LexoNumericComparatorTest(String left, String right, int expectedResult) {
        this.left = left;
        this.right = right;
        this.expectedResult = expectedResult;
    }

    private void verifyResult(String left, String right, int expectedResult) {
        assertEquals(String.format("Expected %1$s to be %3$s %2$s, but it wasn't.",
                left,
                right,
                expectedResult == -1 ? "less than" : (expectedResult == 1 ? "greater than" : "equal to")),
                Integer.signum(expectedResult),
                Integer.signum(comparator.compare(left, right)));
    }

    @Test
    public void runTest() {
        verifyResult(left, right, expectedResult);
        verifyResult(right, left, -expectedResult);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> comparisonParameters() {
        return Arrays.asList(new Object[][] {
                { null, null, 0 },
                { null, "", -1 },
                { "", "", 0 },
                { "", "123", -1 },
                { "123", "123", 0 },
                { "123", "456", -1 },
                { "12", "123", -1 },
                { "012", "12", -1 },
                { "2", "10", -1 },
                { "123abc", "123abc", 0 },
                { "123abc", "456abc", -1 },
                { "12abc", "123abc", -1 },
                { "012abc", "12abc", -1 },
                { "2abc", "10abc", -1 },
                { "123", "abc", -1 },
                { "abc", "abc", 0 },
                { "abc", "def", -1 },
                { "ab", "abc", -1 },
                { "123abc", "123def", -1 },
                { "123ab", "123abc", -1 },
                { "abc123", "abc123", 0 },
                { "abc123", "def123", -1 },
                { "ab123", "abc123", -1 },
                { "abc123", "abc456", -1 },
                { "abc12", "abc123", -1 },
                { "abc012", "abc12", -1 },
                { "abc2", "abc10", -1 },
                { "abc123def", "abc123def", 0 },
                { "abc123def", "abc123ghi", -1 },
                { "abc123de", "abc123def", -1 }
        });
    }
}
