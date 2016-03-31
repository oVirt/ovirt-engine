package org.ovirt.engine.core.common.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.compat.Version;

/**
 * This rule is used to mock {@link Config} values in an easy fashion, without having to resort to Power Mocking.
 *
 * To use it, simple add a {@link MockConfigRule} member to your test, with the {@link org.junit.Rule} annotation.
 * Mocking is done by calling {@link #mockConfigValue(ConfigValues, Object)} or {@link #mockConfigValue(ConfigValues, String, Object)} with the value you need.
 */
public class MockConfigRule extends TestWatcher {

    /** A descriptor for a single config mocking */
    public static class MockConfigDescriptor<T> {

        public MockConfigDescriptor() {
        }
        public MockConfigDescriptor(ConfigValues value, String version, T returnValue) {
            this.value = value;
            this.version = version;
            this.returnValue = returnValue;
        }

        public ConfigValues getValue() {
            return value;
        }

        public String getVersion() {
            return version;
        }

        public T getReturnValue() {
            return returnValue;
        }

        private ConfigValues value;
        private String version;
        private T returnValue;
    }

    private IConfigUtilsInterface origConfUtils;
    private List<MockConfigDescriptor<?>> configs;

    /** Create the rule with no mocking */
    public MockConfigRule() {
        this(Collections.<MockConfigDescriptor<?>> emptyList());
    }

    /** Create the rule, mocking the given configurations */
    public <T> MockConfigRule(MockConfigDescriptor<?>... configs) {
        this(Arrays.asList(configs));
    }

    /** Create the rule, mocking the given configurations */
    public <T> MockConfigRule(List<MockConfigDescriptor<?>> configs) {
        this.configs = new ArrayList<>(configs);
    }

    /** Mock the configuration of a single value - this can be given as an argument to the rule's constructor */
    public static <T> MockConfigDescriptor<T> mockConfig(ConfigValues value, String version, T returnValue) {
        return new MockConfigDescriptor<>(value, version, returnValue);
    }

    public static <T> MockConfigDescriptor<T> mockConfig(ConfigValues value, Version version, T returnValue) {
        return new MockConfigDescriptor<>(value, version.toString(), returnValue);
    }

    /** Mock the default version configuration of a single value - this can be given as an argument to the rule's constructor */
    public static <T> MockConfigDescriptor<T> mockConfig(ConfigValues value, T returnValue) {
        return new MockConfigDescriptor<>(value, ConfigCommon.defaultConfigurationVersion, returnValue);
    }

    public <T> void mockConfigValue(ConfigValues value, T returnValue) {
        mockConfigValue(value, ConfigCommon.defaultConfigurationVersion, returnValue);
    }

    public <T> void mockConfigValue(ConfigValues value, Version version, T returnValue) {
        mockConfigValue(value, version.getValue(), returnValue);
    }

    public <T> void mockConfigValue(MockConfigDescriptor<T> mcd) {
        mockConfigValue(mcd.getValue(), mcd.getVersion(), mcd.getReturnValue());
    }

    private static <T> void mockConfigValue(ConfigValues value, String version, T returnValue) {
        when(Config.getConfigUtils().getValue(value, version)).thenReturn(returnValue);
    }

    @Override
    public void starting(Description description) {
        origConfUtils = Config.getConfigUtils();
        Config.setConfigUtils(mock(IConfigUtilsInterface.class));

        for (MockConfigDescriptor<?> config : configs) {
            mockConfigValue(config.getValue(), config.getVersion(), config.getReturnValue());
        }
    }

    @Override
    public void finished(Description description) {
        Config.setConfigUtils(origConfUtils);
    }
}
