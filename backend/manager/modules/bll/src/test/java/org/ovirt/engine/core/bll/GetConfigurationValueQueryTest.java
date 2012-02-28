package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.utils.RandomUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public class GetConfigurationValueQueryTest extends AbstractUserQueryTest<GetConfigurationValueParameters, GetConfigurationValueQuery<GetConfigurationValueParameters>> {

    @Test
    public void testExecuteQueryUserConfigFiltered() {
        assertQueryExecution(ConfigurationValues.MaxVmsInPool, true, true);
    }

    @Test
    public void testExecuteQueryUserConfigNotFiltered() {
        assertQueryExecution(ConfigurationValues.MaxVmsInPool, false, true);
    }

    @Test
    public void testExecuteQueryAdminConfigFiltered() {
        assertQueryExecution(ConfigurationValues.AdUserName, true, false);
    }

    @Test
    public void testExecuteQueryAdminConfigNotFiltered() {
        assertQueryExecution(ConfigurationValues.AdUserName, false, true);
    }

    private void assertQueryExecution(ConfigurationValues configValue, boolean isFiltered, boolean shouldSucceed) {
        // Mock the parameters
        String version = RandomUtils.instance().nextNumericString(2);
        when(getQueryParameters().getVersion()).thenReturn(version);
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
     * Mocks a call to {@link Config#GetValue(ConfigValues)) and returns the value it should return.
     * @return The mocked value
     */
    private static String mockConfig(String version, ConfigurationValues configurationValues) {
        String returnValue = RandomUtils.instance().nextString(10, true);

        ConfigValues configValues = ConfigValues.valueOf(configurationValues.name());
        mockStatic(Config.class);
        when(Config.GetValue(configValues, version)).thenReturn(returnValue);

        return returnValue;
    }
}
