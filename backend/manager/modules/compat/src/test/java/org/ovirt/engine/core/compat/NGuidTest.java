package org.ovirt.engine.core.compat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests NGuid functionality
 */
public class NGuidTest {

    @Test
    public void testCompareTo() {
        NGuid guid1 = NGuid.createGuidFromString("5b411bc1-c220-4421-9abd-cfa484aecb6e");
        NGuid guid2 = NGuid.createGuidFromString("5b411bc1-c220-4421-9abd-cfa484aecb6f");
        assertTrue(guid1.compareTo(guid2) < 0);
        assertTrue(guid1.compareTo(guid1) == 0);
        assertTrue(guid2.compareTo(guid1) > 0);
    }

}
