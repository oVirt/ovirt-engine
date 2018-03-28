package org.ovirt.engine.core.utils.osinfo;

import static org.junit.Assume.assumeNotNull;

import java.nio.file.Paths;

import org.junit.Test;

public class OsinfoPropertiesParserTest {

    /**
     * Attempt to parse osinfo-defaults.properties.
     * If the file is malformed, a {@link RuntimeException} will be thrown and the test will fail.
     */
    @Test
    public void defaultProperties() {
        String basedir = System.getProperty("basedir");
        assumeNotNull(basedir, "Test isn't run via Maven. Please set the basedir system property");
        OsinfoPropertiesParser.parse(
                Paths.get(basedir, "../../../../packaging/conf/osinfo-defaults.properties").toString());
    }
}
