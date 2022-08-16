package org.ovirt.engine.core.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.config.ConfigValues;

/**
 * A test case to validate that the engine-config.properties files doesn't contain wrong/redundant entries
 */
public class EngineConfigPropertiesTest {
    private static final String PROPERTIES_PATH = System.getProperty("engine-config.properties.production.file");
    private static Set<String> allProps;

    @BeforeAll
    public static void setUpClass() {
        allProps = new HashSet<>();
        try {
            List<String> lines = Files.readAllLines(Path.of(PROPERTIES_PATH));
            for (String line: lines) {
                if (line.contains("=")) {
                    allProps.add(line.split("=")[0].split("\\.")[0]);
                }
            }
        } catch (IOException e) {
            fail(String.format("Failed to load configuration from the engine-config.properties.production.file " +
                    "environment variable set to %s.", PROPERTIES_PATH));
        }
    }

    @Test
    public void testKeysAreRealConfigValues() {
        Set<String> badKeys = new HashSet<>();
        Iterator<String> keyIter = allProps.iterator();
        while (keyIter.hasNext()) {
            String confValueName = keyIter.next();
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
