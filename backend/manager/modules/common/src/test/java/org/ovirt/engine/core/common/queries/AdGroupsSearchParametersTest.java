package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.interfaces.SearchType;

/** A test case for the {@link AdGroupsSearchParameters} class */
public class AdGroupsSearchParametersTest {

    private Random random;

    @Before
    public void setUp() {
        this.random = new Random();
    }

    @Test
    public void testOneArgConstructor() {
        String pattern = "pattern";
        AdGroupsSearchParameters params = new AdGroupsSearchParameters(pattern);

        assertTrue("Wrong pattern", params.getSearchPattern().endsWith(pattern));
        assertEquals("Wrong type", SearchType.AdGroup, params.getSearchTypeValue());
    }

    @Test
    public void testTwoArgConstructor() {
        String pattern = "pattern";
        boolean caseSensitive = this.random.nextBoolean();
        AdGroupsSearchParameters params = new AdGroupsSearchParameters(pattern, caseSensitive);

        assertTrue("Wrong pattern", params.getSearchPattern().endsWith(pattern));
        assertEquals("Wrong type", SearchType.AdGroup, params.getSearchTypeValue());
        assertEquals("Wrong case sensitivity", caseSensitive, params.getCaseSensitive());
    }
}
