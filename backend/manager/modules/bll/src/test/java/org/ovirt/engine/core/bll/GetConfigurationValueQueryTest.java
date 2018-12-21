package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetConfigurationValueQueryTest extends AbstractUserQueryTest<GetConfigurationValueParameters, GetConfigurationValueQuery<GetConfigurationValueParameters>> {

    private static Version version = RandomUtils.instance().pickRandom(Version.ALL);

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.concat(AbstractQueryTest.mockConfiguration(), mockConfigurationForTests());
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationForTests() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.PredefinedVMProperties, version, "some string")
        );
    }

    public static Stream<Arguments> queryExecution() {
        // Return each mocked configuration value permuted with true and false
        return mockConfigurationForTests().flatMap(m -> Stream.of(
                Arguments.of(m.getValue(), m.getReturnValue(), true),
                Arguments.of(m.getValue(), m.getReturnValue(), false))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void queryExecution(ConfigValues configValue, String expected, boolean isFiltered) {

        when(getQueryParameters().getVersion()).thenReturn(version.toString());
        when(getQueryParameters().getConfigValue()).thenReturn(configValue);
        when(getQueryParameters().isFiltered()).thenReturn(isFiltered);

        getQuery().executeQueryCommand();

        Object actual = getQuery().getQueryReturnValue().getReturnValue();

        boolean shouldSucceed = !isFiltered || configValue.nonAdminVisible();
        if (shouldSucceed) {
            assertEquals(expected, actual, "Got wrong expected value for " + configValue);
        } else {
            assertNull(actual, "Should get null result for " + configValue);
        }
    }
}
