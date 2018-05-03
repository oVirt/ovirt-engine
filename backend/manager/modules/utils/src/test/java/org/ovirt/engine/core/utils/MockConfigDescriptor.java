package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;

/** A descriptor for a single config mocking */
public class MockConfigDescriptor<T> {

    private MockConfigDescriptor(ConfigValues value, String version, T returnValue) {
        this.value = value;
        this.version = version;
        this.returnValue = returnValue;
    }

    /** Mock the configuration of a single value - this can be given as an argument to the rule's constructor */
    public static <T> MockConfigDescriptor<T> of(ConfigValues value, String version, T returnValue) {
        return new MockConfigDescriptor<>(value, version, returnValue);
    }

    public static <T> MockConfigDescriptor<T> of(ConfigValues value, Version version, T returnValue) {
        return new MockConfigDescriptor<>(value, version.toString(), returnValue);
    }

    /** Mock the default version configuration of a single value - this can be given as an argument to the rule's constructor */
    public static <T> MockConfigDescriptor<T> of(ConfigValues value, T returnValue) {
        return new MockConfigDescriptor<>(value, ConfigCommon.defaultConfigurationVersion, returnValue);
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
