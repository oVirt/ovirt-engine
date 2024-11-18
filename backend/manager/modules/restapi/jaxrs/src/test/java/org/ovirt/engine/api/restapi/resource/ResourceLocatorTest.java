package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResourceLocatorTest {

    @ParameterizedTest
    @MethodSource("providerUrlsToCheckSuffixExtraction")
    void testResourceLocatorGetPrefix(String source, String expectedResult) {
        assertEquals(expectedResult, ResourceLocator.removePrefix(source));
    }

    private static Stream<Arguments> providerUrlsToCheckSuffixExtraction() {
        var basePart = "http://localhost:8080/ovirt-engine";
        var suffix = "datacenters/1034e9ba-c1a4-442c-8bc9-f7c1c997652b";

        return Stream.of(
                Arguments.of(
                        basePart + "/api/v12/" + suffix,
                        suffix
                ),
                Arguments.of(
                        basePart + "/api/" + suffix,
                        suffix
                ),
                Arguments.of(
                        basePart + "/api/v4/" + suffix,
                        suffix
                ),
                Arguments.of( // Without pattern, method should return the same string.
                        basePart + "/" + suffix,
                        basePart + "/" + suffix
                )
        );
    }
}
