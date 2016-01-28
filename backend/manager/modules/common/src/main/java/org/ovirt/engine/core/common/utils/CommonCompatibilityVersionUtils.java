package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.compat.Version;

public class CommonCompatibilityVersionUtils {
    public static Version getEffective(Version vmCustomCompatibilityVersion,
            Version clusterCompatibilityVersion, Version defaultVersion) {
        if (vmCustomCompatibilityVersion != null) {
            return vmCustomCompatibilityVersion;
        }
        if (clusterCompatibilityVersion != null) {
            return clusterCompatibilityVersion;
        }
        return defaultVersion;
    }
}
