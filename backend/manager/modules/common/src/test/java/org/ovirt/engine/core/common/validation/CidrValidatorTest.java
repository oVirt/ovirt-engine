package org.ovirt.engine.core.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CidrValidatorTest {
    @ParameterizedTest
    @MethodSource
    public void cidrFormatValidation(String cidr, boolean validCidrFormatExpectedResult) {
        assertEquals(validCidrFormatExpectedResult, CidrValidator.getInstance().isCidrFormatValid(cidr),
                "Failed to validate CIDR's Format: " + cidr);
    }

    @ParameterizedTest
    @MethodSource
    public void networkAddressValidation(String cidr, boolean validNetworkAddressExpectedResult) {
        assertEquals(validNetworkAddressExpectedResult, CidrValidator.getInstance().isCidrNetworkAddressValid(cidr),
                "Failed to validate CIDR's network address" + cidr);
    }

    public static Stream<Arguments> cidrFormatValidation() {
        return Stream.of(
                // Bad Format
                Arguments.of(null, false, false),
                Arguments.of("", false, false),
                Arguments.of("?\"?>!", false, false),
                Arguments.of("a", false, false),
                Arguments.of("a.a", false, false),
                Arguments.of("a.a.a", false, false),
                Arguments.of("a.a.a.a", false, false),
                Arguments.of("a.a.a.a/a", false, false),
                Arguments.of("1", false, false),
                Arguments.of("1.1.1.1", false, false),
                Arguments.of("1.1.1.1/", false, false),
                Arguments.of("1000.1.1.1/24", false, false),
                Arguments.of("1.1000.1.1/24", false, false),
                Arguments.of("1.1.1000.1/24", false, false),
                Arguments.of("1.1.1.1000/24", false, false),

                Arguments.of("1.1.1.1/33", false, false),
                Arguments.of("1111.1.1.1/1", false, false),
                Arguments.of("1111.1.1.1/32", false, false),
                Arguments.of("1.1.1.1/1/1", false, false),
                Arguments.of("1.1/1.1/.1/.1", false, false),
                Arguments.of("256.1.1.1/1", false, false),
                Arguments.of("256.1.1.1/32", false, false),
                Arguments.of("256.1.1.1/222222222222222222222222", false, false),
                Arguments.of("255.?.?././", false, false),
                Arguments.of("255?23?1?0/8", false, false),
                Arguments.of("23\22\22\22\22\\", false, false),
                Arguments.of(".................", false, false),
                Arguments.of("././././", false, false),
                Arguments.of("?/?/?/?/", false, false),

                // Not A Network address
                Arguments.of("253.0.0.32/26", true, false),
                Arguments.of("255.255.255.192/25", true, false),
                Arguments.of("255.255.255.16/24", true, false),
                Arguments.of("255.254.192.0/17", true, false),
                Arguments.of("255.255.255.250/16", true, false),
                Arguments.of("255.255.192.17/14", true, false),
                Arguments.of("255.128.0.0/8", true, false),
                Arguments.of("240.255.255.247/3", true, false),
                Arguments.of("224.0.0.0/2", true, false),
                Arguments.of("192.0.0.0/1", true, false),
                Arguments.of("255.255.255.255/0", true, false),

                // valid CIDR
                Arguments.of("255.255.255.255/32", true, true),
                Arguments.of("0.0.0.0/32", true, true),
                Arguments.of("255.255.255.254/31", true, true),
                Arguments.of("255.255.255.248/29", true, true),
                Arguments.of("255.255.255.0/24", true, true),
                Arguments.of("255.255.254.0/23", true, true),
                Arguments.of("255.0.254.0/23", true, true),
                Arguments.of("255.255.0.0/16", true, true),
                Arguments.of("255.252.0.0/14", true, true),
                Arguments.of("255.0.0.0/8", true, true),
                Arguments.of("248.0.0.0/5", true, true),
                Arguments.of("128.0.0.0/1", true, true)
        );
    }

    public static Stream<Arguments> networkAddressValidation() {
        return cidrFormatValidation().filter(a -> (Boolean) a.get()[1]).map(a -> Arguments.of(a.get()[0], a.get()[2]));
    }
}
