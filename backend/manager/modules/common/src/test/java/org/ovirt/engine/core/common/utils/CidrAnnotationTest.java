package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class CidrAnnotationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        validator = ValidationUtils.getValidator();
    }

    @ParameterizedTest
    @MethodSource(value = "namesParams")
    public void checkCidrFormatAnnotation(String cidr,
            IpVersion ipVersion,
            boolean validCidrFormatExpectedResult,
            boolean validNetworkAddressExpectedResult) {

        ExternalSubnet container = createContainer(cidr, ipVersion);
        Set<ConstraintViolation<ExternalSubnet>> result = validator.validate(container);
        if (!validNetworkAddressExpectedResult && validCidrFormatExpectedResult) {
            assertEquals(EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name(),  result.iterator().next().getMessage(),
                    "Failed to validate CIDR's error format: " + container.getCidr());
        } else if (!validCidrFormatExpectedResult) {
            assertEquals(EngineMessage.BAD_CIDR_FORMAT.name(), result.iterator().next().getMessage(),
                    "Failed to validate CIDR's error format: " + container.getCidr());
        } else {
            assertEquals(validCidrFormatExpectedResult, result.isEmpty(),
                    "Failed to validate CIDR's format: " + container.getCidr());
        }

    }

    @ParameterizedTest
    @MethodSource(value = "namesParams")
    public void checkCidrNetworkAddressAnnotation
            (String cidr,
                    IpVersion ipVersion,
                    boolean validCidrFormatExpectedResult,
                    boolean validNetworkAddressExpectedResult) {
        ExternalSubnet container = createContainer(cidr, ipVersion);
        Set<ConstraintViolation<ExternalSubnet>> result = validator.validate(container);
        if (!validCidrFormatExpectedResult) {
            assertEquals(EngineMessage.BAD_CIDR_FORMAT.name(), result.iterator().next().getMessage(),
                    "Failed to validate CIDR's network address error: " + container.getCidr());
        } else if (!validNetworkAddressExpectedResult) {
            assertEquals(EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name(), result.iterator().next().getMessage(),
                    "Failed to validate CIDR's  network address error: " + container.getCidr());
        } else {
            assertEquals(validNetworkAddressExpectedResult, result.isEmpty(),
                    "Failed to validate CIDR's network address: " + container.getCidr());
        }
    }

    public static Stream<Arguments> namesParams() {

        return Stream.of(
                // Bad Format
                Arguments.of("a.a.a.a", IpVersion.IPV4, false, false),
                Arguments.of("abcd:::jklf", IpVersion.IPV6, false, false),

                // Not A Network address
                Arguments.of("253.0.0.32/26", IpVersion.IPV4, true, false),
                Arguments.of("1234:5678::4321/64", IpVersion.IPV6, true, false),

                // valid CIDR
                Arguments.of("255.255.255.255/32", IpVersion.IPV4, true, true),
                Arguments.of("1234:5678:9abc:def0::/64", IpVersion.IPV6, true, true)
        );
    }

    private ExternalSubnet createContainer(String cidr, IpVersion ipVersion) {
        ExternalSubnet container = new ExternalSubnet();
        container.setCidr(cidr);
        container.setIpVersion(ipVersion);
        return container;
    }
}
