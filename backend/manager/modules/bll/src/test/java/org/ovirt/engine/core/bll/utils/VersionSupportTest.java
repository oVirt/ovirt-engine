package org.ovirt.engine.core.bll.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({Config.class})
@RunWith(PowerMockRunner.class)
public class VersionSupportTest {

    private static final Version VALID_VERSION = new Version(1,0);

    @Before
    public void setUp() {
        mockStatic(Config.class);
        Set<Version> versions = new HashSet<Version>();
        versions.add(VALID_VERSION);
        when(Config.GetValue(ConfigValues.SupportedClusterLevels)).thenReturn(versions);
    }

    @Test
    public void nullVersion() {
      assertFalse(VersionSupport.checkVersionSupported(null));
    }

    @Test
    public void validVersion() {
        assertTrue(VersionSupport.checkVersionSupported(VALID_VERSION));
    }

    @Test
    public void invalidVersion() {
        assertFalse(VersionSupport.checkVersionSupported(new Version(2, 0)));
    }



}
