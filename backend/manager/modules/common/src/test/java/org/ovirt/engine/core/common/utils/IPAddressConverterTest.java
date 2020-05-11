package org.ovirt.engine.core.common.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.utils.IPAddressConverter.Ipv4NetworkRange;

public class IPAddressConverterTest {
    private static IPAddressConverter underTest = IPAddressConverter.getInstance();

    @Test
    public void checkIpAddressConversionToLong() {
        runIpAddressConversionToBigInt("255.255.255.255", BigInteger.valueOf(0xffffffff));
        runIpAddressConversionToBigInt("1.1.1.1", BigInteger.valueOf(0x01010101));
        runIpAddressConversionToBigInt("1.255.4.255", BigInteger.valueOf(0x01ff04ff));
        runIpAddressConversionToBigInt("0.128.0.7", BigInteger.valueOf(0x00800007));
        runIpAddressConversionToBigInt("1.2.3.4", BigInteger.valueOf(0x01020304));
        runIpAddressConversionToBigInt("0.0.0.1", BigInteger.valueOf(0x00000001));
        runIpAddressConversionToBigInt("1.1.1.1", BigInteger.valueOf(0x01010101));
        runIpAddressConversionToBigInt("1234:5678:9abc::", new BigInteger(new byte[] {
                (byte) 0x12, (byte) 0x34,
                (byte) 0x56, (byte) 0x78,
                (byte) 0x9a, (byte) 0xbc,
                0, 0,
                0, 0,
                0, 0,
                0, 0,
                0, 0
        }));
        runIpAddressConversionToBigInt("1234:5678:9abc::4321", new BigInteger(new byte[] {
                (byte) 0x12, (byte) 0x34,
                (byte) 0x56, (byte) 0x78,
                (byte) 0x9a, (byte) 0xbc,
                0, 0,
                0, 0,
                0, 0,
                0, 0,
                (byte) 0x43, (byte) 0x21
        }));
    }

    private void runIpAddressConversionToBigInt(String IPAddress, BigInteger expectedIpAddressAsLong) {
        String errorMessage =
                String.format("Fail to convert IPv4 Address: %s to big int.", IPAddress);
        BigInteger actualIpAddressAsLongResult = underTest.convertIpAddressToBigInt(IPAddress);
        runTest(errorMessage, expectedIpAddressAsLong, actualIpAddressAsLongResult);
    }

    private <T> void runTest(String errorMessage, T expected, T actual) {
        assertThat(errorMessage, expected, equalTo(actual));
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
        String actualIpv4Address = underTest.convertPrefixToIPv4Netmask(prefix);
        runTest(errorMessage, expectedIpv4Address, actualIpv4Address);
    }

    @Test
    public void checkInvalidNullStringIpv4AddressConversion() {
        assertThrows(NumberFormatException.class, () -> underTest.convertPrefixToIPv4Netmask(null));
    }

    @Test
    public void checkInvalidStringIpv4AddressConversion() {
        assertThrows(NumberFormatException.class, () -> underTest.convertPrefixToIPv4Netmask("a'"));
    }

    @Test
    public void checkConvertPrefixWithTrailingSlashToIpv4Address(){
        runConvertPrefixToIpv4Address("255.255.0.0", "/16");
    }

    @ParameterizedTest
    @MethodSource
    void testExpansionToAddresses(boolean expansionOk, String address, String netmask, Ipv4NetworkRange ipv4NetworkRange) {
        assertEquals(expansionOk, ipv4NetworkRange.equals(underTest.createIpv4NetworkRange(address, netmask)));
    }

    public static Stream<Arguments> testExpansionToAddresses() {
        final boolean EXPANSION_OK = true;
        return Stream.of(
            Arguments.of(EXPANSION_OK, "192.1.2.12", "255.255.255.0",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("192.1.2.1"), underTest.convertIpAddressToBigInt("192.1.2.254"))),
            Arguments.of(EXPANSION_OK, "10.10.45.55", "255.255.255.192",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("10.10.45.1"), underTest.convertIpAddressToBigInt("10.10.45.62"))),
            Arguments.of(EXPANSION_OK, "130.1.40.0", "255.255.248.0",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("130.1.40.1"), underTest.convertIpAddressToBigInt("130.1.47.254"))),

            Arguments.of(!EXPANSION_OK, "192.1.2.12", "255.255.255.0",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("192.2.2.1"), underTest.convertIpAddressToBigInt("192.2.2.254"))),
            Arguments.of(!EXPANSION_OK, "10.10.45.55", "255.255.255.192",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("10.10.45.1"), underTest.convertIpAddressToBigInt("10.10.45.52"))),
            Arguments.of(!EXPANSION_OK, "10.10.45.55", "255.255.255.192",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("10.10.45.7"), underTest.convertIpAddressToBigInt("10.10.45.62"))),
            Arguments.of(!EXPANSION_OK, "10.10.45.55", "255.255.255.192",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("10.10.44.1"), underTest.convertIpAddressToBigInt("10.10.45.52"))),
            Arguments.of(!EXPANSION_OK, "130.1.40.0", "255.255.248.0",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("130.0.40.1"), underTest.convertIpAddressToBigInt("130.1.47.254"))),
            Arguments.of(!EXPANSION_OK, "130.1.40.0", "255.255.248.0",
                new Ipv4NetworkRange(underTest.convertIpAddressToBigInt("130.1.40.1"), underTest.convertIpAddressToBigInt("130.2.47.254")))
        );
    }
}
