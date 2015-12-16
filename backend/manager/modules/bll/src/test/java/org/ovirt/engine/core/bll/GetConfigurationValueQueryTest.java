package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetConfigurationValueQueryTest extends AbstractUserQueryTest<GetConfigurationValueParameters, GetConfigurationValueQuery<GetConfigurationValueParameters>> {

    @Test
    public void testExecuteQueryUserConfigFiltered() {
        assertQueryExecution(ConfigurationValues.MaxVmsInPool, true, true);
    }

    @Test
    public void testExecuteQueryUserConfigNotFiltered() {
        assertQueryExecution(ConfigurationValues.MaxVmsInPool, false, true);
    }

    private void assertQueryExecution(ConfigurationValues configValue, boolean isFiltered, boolean shouldSucceed) {
        // Mock the parameters
        Version version = RandomUtils.instance().pickRandom(Version.ALL);
        when(getQueryParameters().getVersion()).thenReturn(version.toString());
        when(getQueryParameters().getConfigValue()).thenReturn(configValue);
        when(getQueryParameters().isFiltered()).thenReturn(isFiltered);

        // Mock the config
        String expected = mockConfig(version, configValue);

        getQuery().executeQueryCommand();

        Object actual = getQuery().getQueryReturnValue().getReturnValue();

        if (shouldSucceed) {
            assertEquals("Got wrong expected value for " + configValue, expected, actual);
        } else {
            assertNull("Should get null result for " + configValue, actual);
        }
    }

    /**
     * Mocks a call to {@link org.ovirt.engine.core.common.config.Config#getValue(ConfigValues)} and returns the value
     * it should return.
     * @return The mocked value
     */
    private static String mockConfig(Version version, ConfigurationValues configurationValues) {
        String returnValue = RandomUtils.instance().nextString(10, true);

        ConfigValues configValues = ConfigValues.valueOf(configurationValues.name());
        mcr.mockConfigValue(configValues, version, returnValue);

        return returnValue;
    }
}
