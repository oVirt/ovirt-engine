package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IPAddressPatternTest {

    private Validator validator = ValidationUtils.getValidator();

    @ParameterizedTest
    @MethodSource
    public void checkIPAdress(String address, boolean expectedResult) {
        Set<ConstraintViolation<IPAdress>> validate = validator.validate(new IPAdress(address));
        assertEquals(expectedResult, validate.isEmpty());
    }

    public static Stream<Arguments> checkIPAdress() {
        return Stream.of(
                Arguments.of("0.0.0.0", true ),
                Arguments.of("1.1.1.1", true ),
                Arguments.of("255.255.255.255", true ),
                Arguments.of("192.168.1.1", true ),
                Arguments.of("10.10.1.1", true ),
                Arguments.of("127.0.0.1", true ),
                Arguments.of("", false ),
                Arguments.of(null, true ),
                Arguments.of("10.10.10", false ),
                Arguments.of("10.10", false ),
                Arguments.of( "10", false ),
                Arguments.of( "1.1.1.", false ),
                Arguments.of( "1.1..1", false ),
                Arguments.of( "1..1.1", false ),
                Arguments.of( ".1.1.1", false ),
                Arguments.of( "....", false ),
                Arguments.of( "...", false ),
                Arguments.of( "..", false ),
                Arguments.of( ".", false ),
                Arguments.of( "1.1.1.1.1", false ),
                Arguments.of( "a.10.10.10", false ),
                Arguments.of( "10.a.10.10", false ),
                Arguments.of( "10.10.a.10", false ),
                Arguments.of( "10.10.10.a", false ),
                Arguments.of( "a.a.a.a", false ),
                Arguments.of( "256.10.10.10", false ),
                Arguments.of( "10.256.10.10", false ),
                Arguments.of( "10.10.256.10", false ),
                Arguments.of( "10.10.10.256", false ),
                Arguments.of( "300.10.10.10", false ),
                Arguments.of( "10.300.10.10", false ),
                Arguments.of( "10.10.300.10", false ),
                Arguments.of( "10.10.10.300", false ),
                Arguments.of( "-1.10.10.10", false ),
                Arguments.of( "10.-1.10.10", false ),
                Arguments.of( "10.10.-1.10", false ),
                Arguments.of( "10.10.10.-1", false ),
                Arguments.of( " ", false )
        );
    }

    private static class IPAdress {

        @Pattern(regexp = ValidationUtils.IPV4_PATTERN, message = "IPV4_ADDR_BAD_FORMAT")
        private String address;

        public IPAdress(String address) {
            this.address = address;
        }
    }

}
