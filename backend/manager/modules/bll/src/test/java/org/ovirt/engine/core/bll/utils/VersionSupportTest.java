package org.ovirt.engine.core.bll.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class VersionSupportTest {

    private static final Version VALID_VERSION = new Version(1, 0);

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                Collections.singleton(VALID_VERSION)));
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
