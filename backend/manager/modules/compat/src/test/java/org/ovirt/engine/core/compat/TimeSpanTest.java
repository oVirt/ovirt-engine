package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TimeSpanTest {

    @Test
    public void testBasicConstructors() {
        TimeSpan span = new TimeSpan(10, 10, 10);
        assertEquals(10, span.Hours);
        assertEquals(10, span.Minutes);
        assertEquals(10, span.Seconds);
        assertEquals(0, span.Days);
        assertEquals(0, span.Milliseconds);
    }

    @Test
    public void testNegativeConstructors() {
        TimeSpan span = new TimeSpan(1, -20, -10, 10);
        assertEquals(0, span.Days);
        assertEquals(3, span.Hours);
        assertEquals(50, span.Minutes);
        assertEquals(10, span.Seconds);
        assertEquals(0, span.Milliseconds);
    }

    @Test
    public void testParsing() {
        TimeSpan span = TimeSpan.parse("1");
        assertEquals(1, span.Days);
        span = TimeSpan.parse("-1");
        assertEquals(-1, span.Days);
        span = TimeSpan.parse("1.02:03");
        assertEquals(1, span.Days);
        assertEquals(2, span.Hours);
        assertEquals(3, span.Minutes);
        span = TimeSpan.parse("1.02:03:04.05");
        assertEquals(1, span.Days);
        assertEquals(2, span.Hours);
        assertEquals(3, span.Minutes);
        assertEquals(4, span.Seconds);
        assertEquals(5, span.Milliseconds);
        span = TimeSpan.parse("-1.02:03:04.05");
        assertEquals(-1, span.Days);
        assertEquals(-2, span.Hours);
        assertEquals(-3, span.Minutes);
        assertEquals(-4, span.Seconds);
        assertEquals(-5, span.Milliseconds);
        span = TimeSpan.parse("02:03:04.05");
        assertEquals(0, span.Days);
        assertEquals(2, span.Hours);
        assertEquals(3, span.Minutes);
        assertEquals(4, span.Seconds);
        assertEquals(5, span.Milliseconds);
        span = TimeSpan.parse("-02:03:04.05");
        assertEquals(0, span.Days);
        assertEquals(-2, span.Hours);
        assertEquals(-3, span.Minutes);
        assertEquals(-4, span.Seconds);
        assertEquals(-5, span.Milliseconds);
    }

    @Test
    public void testInvalidParse() {
        assertThrows(IllegalArgumentException.class, ()  -> TimeSpan.parse("1.02.03"));
    }

    @Test
    public void testTryParse() {
        TimeSpan ref = TimeSpan.tryParse("-1.02:03:04.05");
        assertNotNull(ref, "A TimeSpan should be returned");
        assertEquals(-1, ref.Days);
        ref = TimeSpan.tryParse("A Long Time");
        assertNull(ref, "A TimeSpan should not be returned");
    }
}
