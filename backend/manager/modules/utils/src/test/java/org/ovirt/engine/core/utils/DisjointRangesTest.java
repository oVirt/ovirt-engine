package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.LongRange;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DisjointRangesTest {

    @Rule
    public final RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Parameterized.Parameter(0)
    public List<LongRange> inputRanges;
    @Parameterized.Parameter(1)
    public List<LongRange> expectedRanges;

    @Parameterized.Parameters
    public static Object[][] parameters() {
        return new Object[][] {
                { Collections.<LongRange>emptyList(), Collections.<LongRange>emptyList() },
                { Collections.singletonList(pair(1, 2)), Collections.singletonList(pair(1, 2)) },
                { Arrays.asList(pair(1, 2), pair(3, 4)), Arrays.asList(pair(1, 2), pair(3, 4)) },
                { Arrays.asList(pair(1, 3), pair(2, 5)), Collections.singletonList(pair(1, 5)) },
                { Arrays.asList(pair(1, 3), pair(5, 7), pair(6, 7)), Arrays.asList(pair(1, 3), pair(5, 7)) },
                { Arrays.asList(pair(1, 2), pair(5, 7), pair(4, 7)), Arrays.asList(pair(1, 2), pair(4, 7)) },
                { Arrays.asList(pair(1, 3), pair(5, 7), pair(4, 7)), Arrays.asList(pair(1, 3), pair(4, 7)) },
                { Arrays.asList(pair(1, 2), pair(5, 6), pair(9, 11), pair(3, 11)), Arrays.asList(pair(1, 2), pair(3, 11)) },
                { Arrays.asList(pair(1, 2), pair(5, 6), pair(9, 11), pair(3, 12)), Arrays.asList(pair(1, 2), pair(3, 12)) },
                { Arrays.asList(pair(1, 2), pair(5, 6), pair(9, 11), pair(3, 8)), Arrays.asList(pair(1, 2), pair(3, 8), pair(9, 11)) },
                { Arrays.asList(pair(1, 3), pair(5, 6), pair(9, 11), pair(-1, 8)), Arrays.asList(pair(-1, 8), pair(9, 11)) }
        };
    }


    private static LongRange pair(long from, long to) {
        return new LongRange(from, to);
    }

    @Test
    public void test() throws Exception {
        Collections.shuffle(inputRanges, RandomUtils.instance());
        doTest(inputRanges);
    }

    private void doTest(List<LongRange> inputRanges) {
        DisjointRanges disjointRanges = new DisjointRanges();
        disjointRanges.addRanges(inputRanges);

        Collection<LongRange> result = disjointRanges.getRanges();

        assertTrue(CollectionUtils.isEqualCollection(expectedRanges, result));
    }
}
