package org.ovirt.engine.core.common.utils;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.compat.Version;

/**
 * Helper class for {@link FencingPolicy}
 */
public class FencingPolicyHelper {
    /**
     * Returns minimal cluster version, that supports specified fencing policy
     */
    public static Version getMinimalSupportedVersion(FencingPolicy fp) {
        if (fp == null) {
            throw new IllegalArgumentException();
        }

        return Version.ALL.get(0);
    }

    /**
     * Returns true if specified set of cluster levels contains at least one level, which supports fencing policy
     */
    public static boolean isFencingPolicySupported(Set<Version> clusterLevels) {
        return !clusterLevels.isEmpty();
    }
}
