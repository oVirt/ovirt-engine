package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TimeConverterTest {
    @Test
    public void testConvertSecondsToMinutes() {
        long seconds = 1800L;
        Pair<Long, TimeUnit> expectedResults = TimeConverter.autoConvert(seconds, TimeUnit.SECONDS);
        assertEquals(30L, expectedResults.getFirst().longValue());
        assertEquals(TimeUnit.MINUTES, expectedResults.getSecond());
    }

    @Test
    public void testConvertMinutesToHours() {
        long minutes = 720L;
        Pair<Long, TimeUnit> expectedResults = TimeConverter.autoConvert(minutes, TimeUnit.MINUTES);
        assertEquals(12L, expectedResults.getFirst().longValue());
        assertEquals(TimeUnit.HOURS, expectedResults.getSecond());
    }

    @Test
    public void testConvertMillsecondsToMinutes() {
        long milliSeconds = 60000L;
        Pair<Long, TimeUnit> expectedResults = TimeConverter.autoConvert(milliSeconds, TimeUnit.MILLISECONDS);
        assertEquals(1L, expectedResults.getFirst().longValue());
        assertEquals(TimeUnit.MINUTES, expectedResults.getSecond());
    }
}
