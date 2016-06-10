package org.ovirt.engine.core.common.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class IPv4AddressConverterTest {
    private IPAddressConverter underTest = IPv4AddressConverter.getInstance();

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void checkIpAddressConversionToLong() {
        runIpAddressConversionToLong("255.255.255.255", 0xffffffffL);
        runIpAddressConversionToLong("1.1.1.1", 0x01010101);
        runIpAddressConversionToLong("1.255.4.255", 0x01ff04ff);
        runIpAddressConversionToLong("0.128.0.7", 0x00800007);
        runIpAddressConversionToLong("1.2.3.4", 0x01020304);
        runIpAddressConversionToLong("0.0.0.1", 0x00000001);
        runIpAddressConversionToLong("1.1.1.1", 0x01010101);
    }

    private void runIpAddressConversionToLong(String Ipv4Address, long expectedIpAddressAsLong){
        String errorMessage =
                String.format("Fail to convert IPv4 Address: %s to long.", Ipv4Address);
        long actualIpAddressAsLongResult = underTest.convertIpAddressToLong(Ipv4Address);
        runTest(errorMessage, expectedIpAddressAsLong, actualIpAddressAsLongResult);
    }

    private <T> void runTest(String errorMessage, T expected, T actual) {
        errorCollector.checkThat(errorMessage, expected, equalTo(actual));
    }

    @Test
    public void checkConvertPrefixToIpv4Address(){
        runConvertPrefixToIpv4Address("255.255.255.255", "32");
        runConvertPrefixToIpv4Address("0.0.0.0", "0");
        runConvertPrefixToIpv4Address("255.255.255.254", "31");
        runConvertPrefixToIpv4Address("255.254.0.0", "15");
        runConvertPrefixToIpv4Address("254.0.0.0", "7");
    }

    private void runConvertPrefixToIpv4Address(String expectedIpv4Address, String prefix){
        String errorMessage =
                String.format("Fail to convert prefix %s to IPv4 address: %s.", prefix, expectedIpv4Address);
        String actualIpv4Address = underTest.convertPrefixToNetmask(prefix);
        runTest(errorMessage, expectedIpv4Address, actualIpv4Address);
    }

    @Test(expected = NumberFormatException.class)
    public void checkInvalidNullStringIpv4AddressConversion() {
        assertEquals("", underTest.convertPrefixToNetmask(null));
    }

    @Test(expected = NumberFormatException.class)
    public void checkInvalidStringIpv4AddressConversion() {
        assertEquals("", underTest.convertPrefixToNetmask("a'"));
    }

    @Test
    public void checkConvertPrefixWithTrailingSlashToIpv4Address(){
        runConvertPrefixToIpv4Address("255.255.0.0", "/16");
    }
}
