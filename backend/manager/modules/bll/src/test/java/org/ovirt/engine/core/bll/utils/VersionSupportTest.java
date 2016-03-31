package org.ovirt.engine.core.bll.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Version;

public class VersionSupportTest {

    private static final Version VALID_VERSION = new Version(1, 0);

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.SupportedClusterLevels, Collections.singleton(VALID_VERSION))
            );

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
