package org.ovirt.engine.core.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

/**
 * This rule is used to mock {@link Config} values in an easy fashion, without having to resort to Power Mocking.
 *
 * To use it, simple add a {@link MockConfigRule} member to your test, with the {@link @Rule} annotation.
 * Mocking is done by calling {@link #mockConfigValue(ConfigValues, Object)} or {@link #mockConfigValue(ConfigValues, String, Object)} with the value you need.
 *
 */
public class MockConfigRule extends TestWatchman {

    private IConfigUtilsInterface origConfUtils;

    public <T> void mockConfigValue(ConfigValues value, T returnValue) {
        mockConfigValue(value, Config.DefaultConfigurationVersion, returnValue);
    }

    public <T> void mockConfigValue(ConfigValues value, String version, T returnValue) {
        when(Config.getConfigUtils().GetValue(value, version)).thenReturn(returnValue);
    }

    @Override
    public void starting(FrameworkMethod method) {
        origConfUtils = Config.getConfigUtils();
        Config.setConfigUtils(mock(IConfigUtilsInterface.class));
    }

    @Override
    public void finished(FrameworkMethod method) {
        Config.setConfigUtils(origConfUtils);
    }
}
