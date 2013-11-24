package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TimeSpanTest {

    @Test
    public void testBasicConstructors() {
        TimeSpan span = new TimeSpan(10, 10, 10);
        assertEquals("hours1", 10, span.Hours);
        assertEquals("minutes1", 10, span.Minutes);
        assertEquals("seconds1", 10, span.Seconds);
        assertEquals("days1", 0, span.Days);
        assertEquals("miliseconds", 0, span.Milliseconds);
    }

    @Test
    public void testNegativeConstructors() {
        TimeSpan span = new TimeSpan(1, -20, -10, 10);
        assertEquals("days1", 0, span.Days);
        assertEquals("hours1", 3, span.Hours);
        assertEquals("minutes1", 50, span.Minutes);
        assertEquals("seconds1", 10, span.Seconds);
        assertEquals("miliseconds", 0, span.Milliseconds);
    }

    @Test
    public void testParsing() {
        TimeSpan span = TimeSpan.parse("1");
        assertEquals("days1", 1, span.Days);
        span = TimeSpan.parse("-1");
        assertEquals("days2", -1, span.Days);
        span = TimeSpan.parse("1.02:03");
        assertEquals("days3", 1, span.Days);
        assertEquals("hours3", 2, span.Hours);
        assertEquals("min3", 3, span.Minutes);
        span = TimeSpan.parse("1.02:03:04.05");
        assertEquals("days4", 1, span.Days);
        assertEquals("hours4", 2, span.Hours);
        assertEquals("min4", 3, span.Minutes);
        assertEquals("sec4", 4, span.Seconds);
        assertEquals("ms4", 5, span.Milliseconds);
        span = TimeSpan.parse("-1.02:03:04.05");
        assertEquals("days5", -1, span.Days);
        assertEquals("hours5", -2, span.Hours);
        assertEquals("min5", -3, span.Minutes);
        assertEquals("sec5", -4, span.Seconds);
        assertEquals("ms5", -5, span.Milliseconds);
        span = TimeSpan.parse("02:03:04.05");
        assertEquals("days6", 0, span.Days);
        assertEquals("hours6", 2, span.Hours);
        assertEquals("min6", 3, span.Minutes);
        assertEquals("sec6", 4, span.Seconds);
        assertEquals("ms6", 5, span.Milliseconds);
        span = TimeSpan.parse("-02:03:04.05");
        assertEquals("days7", 0, span.Days);
        assertEquals("hours7", -2, span.Hours);
        assertEquals("min7", -3, span.Minutes);
        assertEquals("sec7", -4, span.Seconds);
        assertEquals("ms7", -5, span.Milliseconds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParse() {
        TimeSpan.parse("1.02.03");
    }

    @Test
    public void testTryParse() {
        TimeSpan ref = TimeSpan.tryParse("-1.02:03:04.05");
        assertNotNull("A TimeSpan should be returned", ref);
        assertEquals("days", -1, ref.Days);
        ref = TimeSpan.tryParse("A Long Time");
        assertNull("A TimeSpan should not be returned", ref);
    }
}
