package org.ovirt.engine.core.common.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IpAddressConverterTest {

    final private String ipAddress;
    final private long ipAsLong;

    public IpAddressConverterTest(String ipAddress, long ipAsLong) {
        this.ipAddress = ipAddress;
        this.ipAsLong = ipAsLong;
    }

    @Test
    public void checkIpAddressConversionToLong() {
        long convertResult = IpAddressConverter.getInstance().convertIpToLong(ipAddress);
        assertEquals("Fail to convert ip address " + ipAddress + " expected:\t" + ipAsLong + "got:\t" + convertResult,
                ipAsLong,
                convertResult);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "255.255.255.255", 0xffffffffL },
                { "1.1.1.1", 0x01010101 },
                { "1.255.4.255", 0x01ff04ff },
                { "0.128.0.7", 0x00800007 },
                { "1.2.3.4", 0x01020304 },
                { "0.0.0.1", 0x00000001 },
                { "1.1.1.1", 0x01010101 }, });
    }

}
