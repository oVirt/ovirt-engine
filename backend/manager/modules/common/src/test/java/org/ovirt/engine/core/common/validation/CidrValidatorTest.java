package org.ovirt.engine.core.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CidrValidatorTest {
    @ParameterizedTest
    @MethodSource
    public void cidrFormatValidation(String cidr, boolean isIpv4, boolean validCidrFormatExpectedResult) {
        assertEquals(validCidrFormatExpectedResult, CidrValidator.getInstance().isCidrFormatValid(cidr, isIpv4),
                "Failed to validate CIDR's Format: " + cidr);
    }

    @ParameterizedTest
    @MethodSource
    public void networkAddressValidation(String cidr, boolean isIpv4, boolean validNetworkAddressExpectedResult) {
        assertEquals(validNetworkAddressExpectedResult,
                CidrValidator.getInstance().isCidrNetworkAddressValid(cidr, isIpv4),
                "Failed to validate CIDR's network address " + cidr);
    }

    public static Stream<Arguments> cidrFormatValidation() {
        return Stream.of(
                // Bad Format
                Arguments.of(null, true, false, false),
                Arguments.of("", true, false, false),
                Arguments.of("?\"?>!", true, false, false),
                Arguments.of("a", true, false, false),
                Arguments.of("a.a", true, false, false),
                Arguments.of("a.a.a", true, false, false),
                Arguments.of("a.a.a.a", true, false, false),
                Arguments.of("a.a.a.a/a", true, false, false),
                Arguments.of("1", true, false, false),
                Arguments.of("1.1.1.1", true, false, false),
                Arguments.of("1.1.1.1/", true, false, false),
                Arguments.of("1000.1.1.1/24", true, false, false),
                Arguments.of("1.1000.1.1/24", true, false, false),
                Arguments.of("1.1.1000.1/24", true, false, false),
                Arguments.of("1.1.1.1000/24", true, false, false),

                Arguments.of(null, false, false, false),
                Arguments.of("", false, false, false),
                Arguments.of("?\"?>!", false, false, false),
                Arguments.of("a", false, false, false),
                Arguments.of("a:a", false, false, false),
                Arguments.of("a:a:g", false, false, false),
                Arguments.of("jk::/24", false, false, false),
                Arguments.of("11::/200", false, false, false),
                Arguments.of("20000::/24", false, false, false),
                Arguments.of("25:25:/24", false, false, false),
                Arguments.of(":::/129", false, false, false),

                Arguments.of("1.1.1.1/33", true, false, false),
                Arguments.of("1111.1.1.1/1", true, false, false),
                Arguments.of("1111.1.1.1/32", true, false, false),
                Arguments.of("1.1.1.1/1/1", true, false, false),
                Arguments.of("1.1/1.1/.1/.1", true, false, false),
                Arguments.of("256.1.1.1/1", true, false, false),
                Arguments.of("256.1.1.1/32", true, false, false),
                Arguments.of("256.1.1.1/222222222222222222222222", true, false, false),
                Arguments.of("255.?.?././", true, false, false),
                Arguments.of("255?23?1?0/8", true, false, false),
                Arguments.of("23\22\22\22\22\\", true, false, false),
                Arguments.of(".................", true, false, false),
                Arguments.of("././././", true, false, false),
                Arguments.of("?/?/?/?/", true, false, false),

                // Not A Network address
                Arguments.of("253.0.0.32/26", true, true, false),
                Arguments.of("255.255.255.192/25", true, true, false),
                Arguments.of("255.255.255.16/24", true, true, false),
                Arguments.of("255.254.192.0/17", true, true, false),
                Arguments.of("255.255.255.250/16", true, true, false),
                Arguments.of("255.255.192.17/14", true, true, false),
                Arguments.of("255.128.0.0/8", true, true, false),
                Arguments.of("240.255.255.247/3", true, true, false),
                Arguments.of("224.0.0.0/2", true, true, false),
                Arguments.of("192.0.0.0/1", true, true, false),
                Arguments.of("255.255.255.255/0", true, true, false),

                Arguments.of("f::1/4", false, true, false),
                Arguments.of("ffff::4321/64", false, true, false),
                Arguments.of("1234:5678:9abc:def0::1/64", false, true, false),
                Arguments.of("1234:5678:9abc:def0:1::/64", false, true, false),

                // valid CIDR
                Arguments.of("255.255.255.255/32", true, true, true),
                Arguments.of("0.0.0.0/32", true, true, true),
                Arguments.of("255.255.255.254/31", true, true, true),
                Arguments.of("255.255.255.248/29", true, true, true),
                Arguments.of("255.255.255.0/24", true, true, true),
                Arguments.of("255.255.254.0/23", true, true, true),
                Arguments.of("255.0.254.0/23", true, true, true),
                Arguments.of("255.255.0.0/16", true, true, true),
                Arguments.of("255.252.0.0/14", true, true, true),
                Arguments.of("255.0.0.0/8", true, true, true),
                Arguments.of("248.0.0.0/5", true, true, true),
                Arguments.of("128.0.0.0/1", true, true, true),

                Arguments.of("f000::/4", false, true, true),
                Arguments.of("ffff::/64", false, true, true),
                Arguments.of("1234:5678:9abc::/92", false, true, true),
                Arguments.of("1234:5678:9abc:def0::1/128", false, true, true),
                Arguments.of("::/0", false, true, true),
                Arguments.of("::/1", false, true, true)
        );
    }

    public static Stream<Arguments> networkAddressValidation() {
        return cidrFormatValidation().filter(a -> (Boolean) a.get()[2])
                .map(a -> Arguments.of(a.get()[0], a.get()[1], a.get()[3]));
    }
}
