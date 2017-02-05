package org.ovirt.engine.core.bll.common.comparator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.bll.common.NumericSuffixNormalizer;

@RunWith(Parameterized.class)
public class NumericSuffixNormalizerTest {

    private NumericSuffixNormalizer underTest;

    @Parameterized.Parameter(0)
    public String str1;
    @Parameterized.Parameter(1)
    public String str2;
    @Parameterized.Parameter(2)
    public String expected1;
    @Parameterized.Parameter(3)
    public String expected2;

    @Before
    public void setUp() {
        underTest = new NumericSuffixNormalizer();
    }

    @Test
    public void testNormalize() {
        final List<String> actual = underTest.normalize(str1, str2);
        assertThat(actual.get(0), is(expected1));
        assertThat(actual.get(1), is(expected2));
    }

    @Parameterized.Parameters
    public static Object[][] normalizationParameters() {
        return new Object[][] {
                { null, null, null, null },
                { null, "", null, "" },
                { "", "", "", "" },
                { "", "123", "", "123" },
                { "123", "", "123", "" },
                { "123", "123", "123", "123" },
                { "123", "1", "123", "001" },
                { "1", "123", "001", "123" },
                { "01", "0123", "0001", "0123" },
                { "abc123", "123", "abc123", "123" },
                { "abc123", "1", "abc123", "001" },
                { "abc1", "123", "abc001", "123" },
                { "abc01", "0123", "abc0001", "0123" },
                { "123", "abc123", "123", "abc123" },
                { "123", "abc1", "123", "abc001" },
                { "1", "abc123", "001", "abc123" },
                { "01", "abc0123", "0001", "abc0123" },
                { "abc123", "abc123", "abc123", "abc123" },
                { "abc123", "abc1", "abc123", "abc001" },
                { "abc1", "abc123", "abc001", "abc123" },
                { "abc01", "abc0123", "abc0001", "abc0123" },
                { "abc", "abc123", "abc", "abc123" },
                { "abc123", "abc", "abc123", "abc" },
        };
    }
}
