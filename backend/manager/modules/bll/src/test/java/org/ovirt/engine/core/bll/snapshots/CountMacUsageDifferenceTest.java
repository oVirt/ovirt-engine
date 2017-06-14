package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CountMacUsageDifferenceTest {

    @Parameterized.Parameter(0)
    public List<String> from;

    @Parameterized.Parameter(1)
    public List<String> to;

    @Parameterized.Parameter(2)
    public List<String> extraMacs;

    @Parameterized.Parameter(3)
    public List<String> missingMacs;

    @Parameterized.Parameter(4)
    public boolean toMacsAreExpectedToBeAlreadyAllocated;

    private CountMacUsageDifference countMacUsageDifference;

    @Parameterized.Parameters(name = "Iteration#{index}: comparing {0} to {1}")
    public static Object[][] data() {
        return new Object[][] {
            //                 from                to              extraMacs      missingMacs     toMacsAlreadyAllocated
            new Object[]{ stringsRange(1, 3), stringsRange(2, 4), stringsFrom(1), stringsFrom(4), false},
            new Object[]{ stringsRange(1, 1), stringsRange(1, 1), stringsFrom(), stringsFrom(), false},
            new Object[]{ stringsRange(1, 1), stringsRange(2, 2), stringsFrom(1), stringsFrom(2), false},
            new Object[]{ stringsRange(2, 2), stringsRange(1, 1), stringsFrom(2), stringsFrom(1), false},

            new Object[]{ stringsFrom(1, 1, 1, 2), stringsFrom(1, 1, 2, 2, 2), stringsFrom(1), stringsFrom(2, 2), false},

            new Object[]{ stringsFrom(1, null), stringsFrom(1, null), stringsFrom(), stringsFrom(), false},



            new Object[]{ stringsRange(1, 3), stringsRange(2, 4), stringsFrom(1), stringsFrom(), true},
            new Object[]{ stringsRange(1, 1), stringsRange(1, 1), stringsFrom(), stringsFrom(), true},
            new Object[]{ stringsRange(1, 1), stringsRange(2, 2), stringsFrom(1), stringsFrom(), true},
            new Object[]{ stringsRange(2, 2), stringsRange(1, 1), stringsFrom(2), stringsFrom(), true},

            new Object[]{ stringsFrom(1, 1, 1, 2), stringsFrom(1, 1, 2, 2, 2), stringsFrom(1), stringsFrom(), true},

            new Object[]{ stringsFrom(1, null), stringsFrom(1, null), stringsFrom(), stringsFrom(), true}
        };
    }

    private static List<String> stringsFrom(Integer ... strings) {
        return Arrays.stream(strings).map(e -> e == null ? null : e.toString()).collect(Collectors.toList());
    }

    private static List<String> stringsRange(int from, int to) {
        return IntStream.rangeClosed(from, to).boxed().map(Object::toString).collect(Collectors.toList());
    }

    @Before
    public void setUp() {
        countMacUsageDifference =
                new CountMacUsageDifference(from.stream(), to.stream(), toMacsAreExpectedToBeAlreadyAllocated);
    }

    @Test
    public void testExtraMacs() {
        assertThat(countMacUsageDifference.getExtraMacs(), CoreMatchers.equalTo(extraMacs));
    }

    @Test
    public void testMissingMacs() {
        assertThat(countMacUsageDifference.getMissingMacs(), CoreMatchers.equalTo(missingMacs));
    }
}
