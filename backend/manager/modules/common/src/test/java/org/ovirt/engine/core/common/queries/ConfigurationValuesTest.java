package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** A test case for the {@link ConfigurationValues} class */
@RunWith(Parameterized.class)
public class ConfigurationValuesTest {

    /** The tested value */
    private ConfigurationValues value;

    public ConfigurationValuesTest(ConfigurationValues value) {
        this.value = value;
    }

    @Parameters
    public static Object[] data() {
        return ConfigurationValues.values();
    }

    @Test
    public void testGetValue() {
        assertEquals("Wrong getValue() for " + value, value.ordinal(), value.getValue());
    }

    @Test
    public void testForValue() {
        assertEquals("Wrong forValue() for " + value, value, ConfigurationValues.forValue(value.getValue()));
    }

    @Test
    public void testIsAdmin() {
        assertEquals("Wrong isAdmin() for " + value,
                value.isAdmin(),
                value.getConfigAuthType() == ConfigurationValues.ConfigAuthType.Admin);
    }
}
