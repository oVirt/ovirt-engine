package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class TicketingTest {
    @Test
    public void testNoRepeats() {
        String sample = Ticketing.generateOTP();
        for (int x = 0; x < 1000; x++) {
            String other = Ticketing.generateOTP();
            assertNotEquals(sample, other);
        }
    }
}
