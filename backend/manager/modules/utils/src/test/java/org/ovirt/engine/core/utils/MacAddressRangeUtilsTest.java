package org.ovirt.engine.core.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;

import org.apache.commons.lang.math.LongRange;
import org.junit.jupiter.api.Test;

public class MacAddressRangeUtilsTest {

    @Test
    public void testMacToString() {
        assertThat(MacAddressRangeUtils.macToString(112910729216L), is("00:1a:4a:01:00:00"));
    }

    @Test
    public void testMacToLong() {
        assertThat(MacAddressRangeUtils.macToLong("00:1a:4a:01:00:00"), is(112910729216L));
    }

    protected void testRange(String start, String end, long expectedStart, long expectedEnd) {
        final Collection<LongRange> ranges = MacAddressRangeUtils.parseRangeString(start + '-' + end);
        final LongRange firstRange = ranges.iterator().next();
        assertThat(firstRange.getMinimumLong(), equalTo(expectedStart));
        assertThat(firstRange.getMaximumLong(), equalTo(expectedEnd));
    }

    @Test
    public void testParseValidRange() {
        String start = "00:1a:4a:01:00:00";
        String end = "00:1a:4a:FF:FF:FF";
        final long expectedStart = MacAddressRangeUtils.macToLong(start);
        final long expectedEnd = MacAddressRangeUtils.macToLong(end);

        testRange(start, end, expectedStart, expectedEnd);
    }

    @Test
    public void testParseHugeRange() {
        String start = "00:1a:4a:01:00:00";
        String end = "00:FF:FF:FF:FF:FF";
        final long expectedStart = MacAddressRangeUtils.macToLong(start);
        final long expectedEnd = MacAddressRangeUtils.macToLong(start) + Integer.MAX_VALUE - 1;

        testRange(start, end, expectedStart, expectedEnd);
    }

    @Test
    public void testParseClippedEndRange() {
        String start = "00:FF:FF:FF:FF:FF";
        String end = "01:1a:4a:01:00:00";
        final long expectedStartEnd = MacAddressRangeUtils.macToLong(start);

        testRange(start, end, expectedStartEnd, expectedStartEnd);
    }

    @Test
    public void testParseClippedStartRange() {
        String start = "01:FF:FF:FF:FF:FF";
        String end = "02:00:00:00:00:00";
        final long expectedStartEnd = MacAddressRangeUtils.macToLong(end);

        testRange(start, end, expectedStartEnd, expectedStartEnd);
    }

    @Test
    public void testParseWholeRangeMulticast() {
        String start = "01:FF:FF:FF:FF:00";
        String end = "01:FF:FF:FF:FF:00";

        final Collection<LongRange> ranges = MacAddressRangeUtils.parseRangeString(start + '-' + end);
        assertThat(ranges.isEmpty(), is(true));
    }
}
