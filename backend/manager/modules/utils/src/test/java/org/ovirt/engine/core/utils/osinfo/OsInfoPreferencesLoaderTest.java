package org.ovirt.engine.core.utils.osinfo;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class OsInfoPreferencesLoaderTest {

    @BeforeClass
    public static void init() throws URISyntaxException {
        Path directoryPath = new File(OsInfoPreferencesLoader.class.getResource("/osinfo.conf.d").toURI().getPath()).toPath();
        OsInfoPreferencesLoader.INSTANCE.init(directoryPath);
    }
    @Test
    public void testLoad() throws Exception {
        Assert.assertTrue(OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .nodeExists("/os/default/resources/maximum/ram"));
        Assert.assertEquals(32000, OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .node("/os/default/resources/maximum/ram").getInt("value.3.1", -1));
    }

    @Test
    public void testLoadOverridingFiles() throws Exception {
        Assert.assertEquals("spice/qxl", OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .node("/os/default/devices/display/protocols")
                .get("value", "spice/qxl"));
    }
}
