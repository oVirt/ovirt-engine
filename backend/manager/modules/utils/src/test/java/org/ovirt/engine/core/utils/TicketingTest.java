package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.RepeatedTest;

public class TicketingTest {
    private static final String sample = Ticketing.generateOTP();

    @RepeatedTest(1000)
    public void testNoRepeats() {
        String other = Ticketing.generateOTP();
        assertNotEquals(sample, other);

    }
}
