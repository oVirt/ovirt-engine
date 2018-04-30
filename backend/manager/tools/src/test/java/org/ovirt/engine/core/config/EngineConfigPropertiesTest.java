package org.ovirt.engine.core.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.config.ConfigValues;

/**
 * A test case to validate that the engine-config.properties files doesn't contain wrong/redundant entries
 */
public class EngineConfigPropertiesTest {
    private static final String PROPERTIES_PATH = System.getProperty("engine-config.properties.production.file");
    private static PropertiesConfiguration pc;

    @BeforeAll
    public static void setUpClass() {
        try {
            pc = new PropertiesConfiguration(PROPERTIES_PATH);
        } catch (ConfigurationException e) {
            fail(String.format("Failed to load configuration from the engine-config.properties.production.file " +
                    "environment variable set to %s.", PROPERTIES_PATH));
        }
    }

    @Test
    public void testKeysAreRealConfigValues() {
        Set<String> badKeys = new HashSet<>();
        Iterator<String> keyIter = pc.getKeys();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            String confValueName = key.split("\\.")[0];

            try {
                ConfigValues.valueOf(confValueName);
            } catch (IllegalArgumentException e) {
                badKeys.add(confValueName);
            }
        }

        assertThat(String.format("Found keys in %s that don't have corresponding ConfigValues", PROPERTIES_PATH),
                badKeys,
                empty());
    }
}
