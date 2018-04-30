package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    public void testToString() {
        assertEquals("1.0", new Version("1.0").toString());
        assertEquals("1.0", new Version("1.0").toString());
        assertEquals("1.2.3", new Version(1, 2, 3).toString());
    }

    @Test
    public void equals() {
        assertEquals(new Version(), new Version());
        assertEquals(new Version(1, 2), new Version(1, 2));
        assertEquals(new Version(1, 2), new Version("1.2"));
        assertEquals(new Version(1, 2, 3), new Version("1.2.3"));
        assertEquals(new Version(1, 2, 3, 4), new Version("1.2.3.4"));
        // nulls and other data types
        assertNotEquals(new Version(), null);
        assertNotEquals(new Version(), "foo");
        assertNotEquals(new Version(), 1d);
    }

    @Test
    public void compare() {
        assertTrue(Version.v3_6.compareTo(Version.v4_0) < 0);
        assertTrue(Version.v4_0.compareTo(Version.v3_6) > 0);
        assertEquals(0, Version.v3_6.compareTo(new Version("3.6")));
    }

    @Test
    public void biggerThan() {
        assertFalse(Version.v3_6.greater(Version.v4_0));
        assertTrue(Version.v4_0.greater(Version.v3_6));
    }
    @Test
    public void smallerThan() {
        assertTrue(Version.v3_6.less(Version.v4_0));
        assertFalse(Version.v4_0.less(Version.v3_6));
    }

    @Test
    public void biggerThanOrEquals() {
        assertFalse(Version.v3_6.greaterOrEquals(Version.v4_0));
        assertTrue(Version.v4_0.greaterOrEquals(Version.v3_6));
        assertTrue(Version.v4_0.greaterOrEquals(new Version("3.6")));
    }
    @Test
    public void smallerThanOrEquals() {
        assertTrue(Version.v3_6.lessOrEquals(new Version("3.6")));
        assertTrue(Version.v3_6.lessOrEquals(Version.v4_0));
        assertFalse(Version.v4_0.lessOrEquals(Version.v3_6));
    }
}
