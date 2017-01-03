package org.ovirt.engine.core.bll.common.comparator;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.businessentities.Nameable;

@RunWith(Parameterized.class)
public class NumericSuffixNameableComparatorTest {

    private NumericSuffixNameableComparator underTest;

    private final Nameable nameable1;
    private final Nameable nameable2;
    private final Matcher<Integer> expected;

    public NumericSuffixNameableComparatorTest(String name1, String name2, int expected) {
        this.nameable1 = new MyNameable(name1);
        this.nameable2 = new MyNameable(name2);
        if (expected == 0) {
            this.expected = is(0);
        } else if (expected > 0) {
            this.expected = greaterThan(0);
        } else {
            this.expected = lessThan(0);
        }
    }

    @Before
    public void setUp() {
        underTest = new NumericSuffixNameableComparator();
    }

    @Test
    public void testCompare() {
        assertThat(underTest.compare(nameable1, nameable2), expected);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> comparisonParameters() {
        return Arrays.asList(new Object[][] {
                { null, null, 0 },
                { null, "", -1 },
                { "", "", 0 },
                { "", "123", -1 },
                { "123", "", 1 },
                { "123", "123", 0 },
                { "123", "1", 1 },
                { "1", "123", -1 },
                { "01", "0123", -1 },
                { "abc123", "123", 1 },
                { "abc123", "1", 1 },
                { "abc1", "123", 1 },
                { "abc01", "0123", 1 },
                { "123", "abc123", -1 },
                { "123", "abc1", -1 },
                { "1", "abc123", -1 },
                { "01", "abc0123", -1 },
                { "abc123", "abc123", 0 },
                { "abc123", "abc1", 1 },
                { "abc1", "abc123", -1 },
                { "abc01", "abc0123", -1 },
                { "abc", "abc123", -1 },
                { "abc123", "abc", 1 },
        });
    }

    private static class MyNameable implements Nameable {
        private final String name;

        private MyNameable(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
