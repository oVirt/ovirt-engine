package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class TicketingTest {
    @Test
    public void testNoRepeats() {
        String sample = Ticketing.generateOTP();
        for (int x = 0; x < 1000; x++) {
            String other = Ticketing.generateOTP();
            assertNotSame(sample, other);
        }
    }
}
