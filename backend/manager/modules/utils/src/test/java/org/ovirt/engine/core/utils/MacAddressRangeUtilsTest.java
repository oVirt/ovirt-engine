package org.ovirt.engine.core.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MacAddressRangeUtilsTest {

    @Test
    public void testMacToString() throws Exception {
        assertThat(MacAddressRangeUtils.macToString(112910729216L), is("00:1a:4a:01:00:00"));
    }

    @Test
    public void testMacToLong() throws Exception {
        assertThat(MacAddressRangeUtils.macToLong("00:1a:4a:01:00:00"), is(112910729216L));
    }
}
