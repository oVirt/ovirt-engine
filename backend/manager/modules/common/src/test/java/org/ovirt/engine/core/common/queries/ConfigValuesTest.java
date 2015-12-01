package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigValuesTest {
    private static final Logger log = LoggerFactory.getLogger(ConfigValuesTest.class);
    private static final Set<String> configValuesEnumNames = getConfigValuesEnumNames();

    private static Set<String> getConfigValuesEnumNames() {
        Set<String> configValuesEnumNames = new HashSet<>();
        for (ConfigValues configValue : ConfigValues.values()) {
            configValuesEnumNames.add(configValue.toString());
        }
        return configValuesEnumNames;
    }

    @Test
    public void findMissingEnumNames() {
        // Find missing ConfigurationValues enum names in ConfigValues enum
        Set<ConfigurationValues> missingKeys = new TreeSet<>();
        for (ConfigurationValues configValue : ConfigurationValues.values()) {
            if (!configValuesEnumNames.contains(configValue.toString())) {
                log.error("Found missing key: {}", configValue);
                missingKeys.add(configValue);
            }
        }

        assertTrue("Found missing keys: " + missingKeys, missingKeys.isEmpty());
    }
}
