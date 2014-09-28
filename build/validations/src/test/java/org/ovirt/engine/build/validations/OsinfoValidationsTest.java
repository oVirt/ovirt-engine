package org.ovirt.engine.build.validations;

import org.junit.Test;
import org.ovirt.engine.core.utils.osinfo.OsinfoPropertiesParser;

public class OsinfoValidationsTest {

    @Test
    public void parse() throws Exception {
        try {
            OsinfoPropertiesParser.parse(System.getProperty("osinfo.properties"));
        } catch (Exception e) {
            System.err.println("Ovirt-engine will fail to load with a broken osinfo properties file.");
            System.err.println("Please fix the properties file or Osinfo.g4 grammer under org.ovirt.engine.core.utils.");
            throw e;
        }
    }
}
