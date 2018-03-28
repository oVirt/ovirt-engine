package org.ovirt.engine.core.utils.osinfo;

import static org.junit.Assume.assumeNotNull;

import java.nio.file.Paths;

import org.junit.Test;

public class OsinfoPropertiesParserTest {

    @Test
    public void defaultProperties() {
        try {
            String basedir = System.getProperty("basedir");
            assumeNotNull(basedir, "Test isn't run via Maven. Please set the basedir system property");
            OsinfoPropertiesParser.parse(
                    Paths.get(basedir, "../../../../packaging/conf/osinfo-defaults.properties").toString());
        } catch (Exception e) {
            System.err.println("oVirt-engine will fail to load with a broken osinfo properties file.");
            System.err.println("Please fix the properties file or osinfo.jj grammar under org.ovirt.engine.core.utils.osinfo.");
            throw e;
        }
    }
}
