package org.ovirt.engine.core.config.entity.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.config.ConfigValues;

public class FenceConfigValueHelperTest {

    private FenceConfigValueHelper validator = new FenceConfigValueHelper();

    @ParameterizedTest
    @MethodSource
    public void validateFenceAgentMappingConfig(String fenceAgentMapping, boolean expectedResult) {
        assertEquals(expectedResult, validator.validate(ConfigValues.FenceAgentMapping.name(), fenceAgentMapping).isOk());
    }

    public static Stream<Arguments> validateFenceAgentMappingConfig() {
        return Stream.concat(badParams(), Stream.of(
                Arguments.of("agent1=agent2", true),
                Arguments.of("agent1=agent2,agent3=agent4", true),
                Arguments.of("agent1", false),
                Arguments.of("agent1=", false)
        ));
    }

    @ParameterizedTest
    @MethodSource
    public void validateFenceAgentDefaultParamsConfig(String fenceAgentDefault, boolean expectedResult) {
        assertEquals(expectedResult, validator.validate(ConfigValues.FenceAgentDefaultParams.name(), fenceAgentDefault).isOk());
    }

    public static Stream<Arguments> validateFenceAgentDefaultParamsConfig() {
        return Stream.concat(badParams(), Stream.of(
                Arguments.of("agent1:key1=val1", true),
                Arguments.of("agent1:key1=val1,flag=1", true),
                Arguments.of("key1=val1,flag=1", false),
                Arguments.of("agent1:key1", false)
        ));
    }

    @ParameterizedTest
    @MethodSource
    public void validateVdsFenceOptionMappingConfig(String vdsFenceOptionMapping, boolean expectedResult) {
        assertEquals(expectedResult, validator.validate(ConfigValues.VdsFenceOptionMapping.name(), vdsFenceOptionMapping).isOk());
    }

    public static Stream<Arguments> validateVdsFenceOptionMappingConfig() {
        return Stream.concat(badParams(), Stream.of(
                Arguments.of("agent1:secure=secure", true),
                Arguments.of("agent1:secure=secure,port=port;agent2:", true),
                Arguments.of("agent1:secure=0,key", false),
                Arguments.of("agent1:slot=slot,port", false)
        ));
    }

    @ParameterizedTest
    @MethodSource
    public void validateVdsFenceTypeConfig(String vdsFenceType, boolean expectedResult) {
        assertEquals(expectedResult, validator.validate(ConfigValues.VdsFenceType.name(), vdsFenceType).isOk());
    }

    public static Stream<Arguments> validateVdsFenceTypeConfig() {
        return Stream.concat(badParams(), Stream.of(
                Arguments.of("agent1", true),
                Arguments.of("agent1,agent2,agent3", true),
                Arguments.of("agent1,,", false),
                Arguments.of("agent1,,agent2", false)
        ));
    }

    private static Stream<Arguments> badParams() {
        return Stream.of(null, "", "").map(arg -> Arguments.of(arg, false));
    }
}
