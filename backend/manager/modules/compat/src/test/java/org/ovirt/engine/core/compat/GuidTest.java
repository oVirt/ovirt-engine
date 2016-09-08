package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

    @Test
    public void testStringCreation() {
        Guid guid = Guid.newGuid();
        Guid guidFromString = new Guid(guid.toString());
        assertEquals(guidFromString, guid);

        guidFromString = Guid.createGuidFromString(guid.toString());
        assertEquals(guidFromString, guid);
        guidFromString = Guid.createGuidFromString(null);
        assertNull(guidFromString);

        guidFromString = Guid.createGuidFromStringDefaultEmpty(guid.toString());
        assertEquals(guidFromString, guid);
        guidFromString = Guid.createGuidFromStringDefaultEmpty(null);
        assertEquals(Guid.Empty, guidFromString);
    }

    @Test
    public void testToByteArray() {
        final byte[] byteArray = Guid.newGuid().toByteArray();
        assertNotNull(byteArray);
        assertEquals(16, byteArray.length);
    }

    @Test
    public void testToByteArrayAllNull() {
        final byte[] allNullArray = Guid.Empty.toByteArray();
        assertNotNull(allNullArray);
        for (int i = 0; i < 16; i++) {
            assertEquals(0, allNullArray[i]);
        }
    }
}
