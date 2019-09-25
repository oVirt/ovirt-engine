package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

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
        assertTrue(Version.v4_2.compareTo(Version.v4_3) < 0);
        assertTrue(Version.v4_3.compareTo(Version.v4_2) > 0);
        assertEquals(0, Version.v4_3.compareTo(new Version("4.3")));
    }

    @Test
    public void biggerThan() {
        assertFalse(Version.v4_2.greater(Version.v4_3));
        assertTrue(Version.v4_3.greater(Version.v4_2));
    }
    @Test
    public void smallerThan() {
        assertTrue(Version.v4_2.less(Version.v4_3));
        assertFalse(Version.v4_3.less(Version.v4_2));
    }

    @Test
    public void biggerThanOrEquals() {
        assertFalse(Version.v4_2.greaterOrEquals(Version.v4_3));
        assertTrue(Version.v4_3.greaterOrEquals(Version.v4_2));
        assertTrue(Version.v4_3.greaterOrEquals(new Version("4.3")));
    }
    @Test
    public void smallerThanOrEquals() {
        assertTrue(Version.v4_3.lessOrEquals(new Version("4.3")));
        assertTrue(Version.v4_2.lessOrEquals(Version.v4_3));
        assertFalse(Version.v4_3.lessOrEquals(Version.v4_2));
    }
    @Test
    public void lessOrEqualsCollection() {
        assertFalse(Version.v4_3.lessOrEquals(Collections.singletonList(Version.v4_2)));
        assertFalse(Version.v4_4.lessOrEquals(Collections.singletonList(Version.v4_2)));
        assertTrue(Version.v4_3.lessOrEquals(Collections.singletonList(Version.v4_3)));
        assertTrue(Version.v4_3.lessOrEquals(Arrays.asList(Version.v4_2, Version.v4_3)));
        assertTrue(Version.v4_3.lessOrEquals(Arrays.asList(Version.v4_2, Version.v4_3, Version.v4_4)));
        assertTrue(Version.v4_3.lessOrEquals(Arrays.asList(Version.v4_3, Version.v4_4)));
        assertTrue(Version.v4_4.lessOrEquals(Version.ALL));
    }
}
