package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

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
    public static Collection<Object[]> data() {
        ConfigurationValues[] allValues = ConfigurationValues.values();
        int numValues = allValues.length;
        Object[][] params = new Object[numValues][1];
        for (int i = 0; i < numValues; ++i) {
            params[i][0] = allValues[i];
        }

        return Arrays.asList(params);
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
