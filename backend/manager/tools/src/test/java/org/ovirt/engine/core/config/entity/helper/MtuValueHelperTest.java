package org.ovirt.engine.core.config.entity.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.config.entity.ConfigKeyFactory;

public class MtuValueHelperTest {

    private MtuValueHelper validator = new MtuValueHelper();

    @ParameterizedTest
    @MethodSource
    public void validateRanges(String values, boolean expectedResult) {
        assertEquals(expectedResult,
                validator.validate(ConfigKeyFactory.getInstance().generateBlankConfigKey(ConfigValues.DefaultMTU.name(), "Mtu"), values)
                        .isOk());
    }

    public static Stream<Arguments> validateRanges() {
        return Stream.of(
                Arguments.of("0", true),
                Arguments.of("68", true),
                Arguments.of("1500", true),
                Arguments.of("15000", true),
                Arguments.of(String.valueOf(Integer.MAX_VALUE), true),
                Arguments.of("1", false),
                Arguments.of("-1500", false),
                Arguments.of("67", false),
                Arguments.of("6aa", false),
                Arguments.of("abc", false),
                Arguments.of(String.valueOf(Integer.MAX_VALUE) + "1", false),
                Arguments.of("10-20", false),
                Arguments.of("#", false),
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of(" ", false)
        );
    }
}
