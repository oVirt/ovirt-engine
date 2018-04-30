package org.ovirt.engine.core.utils.osinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OsInfoPreferencesLoaderTest {

    @BeforeAll
    public static void init() throws URISyntaxException {
        Path directoryPath = new File(OsInfoPreferencesLoader.class.getResource("/osinfo.conf.d").toURI().getPath()).toPath();
        OsInfoPreferencesLoader.INSTANCE.init(directoryPath);
    }
    @Test
    public void testLoad() throws Exception {
        assertTrue(OsInfoPreferencesLoader.INSTANCE.getPreferences().nodeExists("/os/default/resources/maximum/ram"));
        assertEquals(32000, OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .node("/os/default/resources/maximum/ram").getInt("value.3.1", -1));
    }

    @Test
    public void testLoadOverridingFiles() {
        assertEquals("spice/qxl", OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .node("/os/default/devices/display/protocols")
                .get("value", "spice/qxl"));
    }
}
