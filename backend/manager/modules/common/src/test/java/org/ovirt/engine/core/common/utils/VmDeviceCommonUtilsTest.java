package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.compat.Version;

public class VmDeviceCommonUtilsTest {

    @Test
    public void isOldClusterLevel() {
        assertTrue(VmDeviceCommonUtils.isOldClusterVersion(Version.v3_0));
        assertFalse(VmDeviceCommonUtils.isOldClusterVersion(Version.v3_1));
        assertFalse(VmDeviceCommonUtils.isOldClusterVersion(new Version("4.0")));
    }
}
