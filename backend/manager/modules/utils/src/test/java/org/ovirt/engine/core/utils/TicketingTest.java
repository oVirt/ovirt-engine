package org.ovirt.engine.core.utils;

import junit.framework.TestCase;

public class TicketingTest extends TestCase {
    public void testNoRepeats() {
        String sample = Ticketing.GenerateOTP();
        for (int x = 0; x < 1000; x++) {
            String other = Ticketing.GenerateOTP();
            assertNotSame(sample, other);
        }
    }
}
