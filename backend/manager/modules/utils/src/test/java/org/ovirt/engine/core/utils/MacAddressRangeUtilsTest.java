package org.ovirt.engine.core.utils;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class MacAddressRangeUtilsTest {

    @Test
    public void testMacToString() throws Exception {
        final String macString = "00:1a:4a:01:00:00";
        final long macLong = 112910729216L;

        Assert.assertThat(MacAddressRangeUtils.macToString(macLong), CoreMatchers.is(macString));
    }

}
