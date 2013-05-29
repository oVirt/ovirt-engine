package org.ovirt.engine.core.compat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests Guid functionality
 */
public class GuidTest {

    @Test
    public void testCompareTo() {
        Guid guid1 = new Guid("5b411bc1-c220-4421-9abd-cfa484aecb6e");
        Guid guid2 = new Guid("5b411bc1-c220-4421-9abd-cfa484aecb6f");
        assertTrue(guid1.compareTo(guid2) < 0);
        assertTrue(guid1.compareTo(guid1) == 0);
        assertTrue(guid2.compareTo(guid1) > 0);
    }

}
