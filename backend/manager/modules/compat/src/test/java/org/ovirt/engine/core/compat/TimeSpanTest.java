package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParse() {
        TimeSpan.parse("1.02.03");
    }

    @Test
    public void testTryParse() {
        TimeSpan ref = TimeSpan.tryParse("-1.02:03:04.05");
        assertNotNull("A TimeSpan should be returned", ref);
        assertEquals(-1, ref.Days);
        ref = TimeSpan.tryParse("A Long Time");
        assertNull("A TimeSpan should not be returned", ref);
    }
}
