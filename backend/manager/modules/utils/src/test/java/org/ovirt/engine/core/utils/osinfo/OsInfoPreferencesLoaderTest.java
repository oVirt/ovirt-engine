package org.ovirt.engine.core.utils.osinfo;

import java.io.File;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class OsInfoPreferencesLoaderTest {

    @BeforeClass
    public static void init() {
        Path directoryPath = new File(OsInfoPreferencesLoader.class.getResource("/osinfo.conf.d").getPath()).toPath();
        OsInfoPreferencesLoader.INSTANCE.init(directoryPath);
    }
    @Test
    public void testLoad() throws Exception {
        Assert.assertTrue(OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .nodeExists("/os/default/resources/maximum/ram"));
        Assert.assertEquals(32000,OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .node("/os/default/resources/maximum/ram").getInt("value.3.1",-1));
    }

    @Test
    public void testLoadOverridingFiles() throws Exception {
        Assert.assertEquals(false, OsInfoPreferencesLoader.INSTANCE.getPreferences()
                .node("/os/default/spiceSupport")
                .getBoolean("value", true));
    }
}
