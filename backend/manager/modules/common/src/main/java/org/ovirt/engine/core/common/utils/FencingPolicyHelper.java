package org.ovirt.engine.core.common.utils;

import java.util.Set;

import org.ovirt.engine.core.common.FeatureSupported;
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

        Version ver = Version.ALL.get(0);
        if (fp.isSkipFencingIfSDActive()) {
            for (Version v : Version.ALL) {
                if (FeatureSupported.isSkipFencingIfSDActiveSupported(v)) {
                    ver = v;
                    break;
                }
            }
        }
        return ver;
    }

    /**
     * Returns true if specified set of cluster levels contains at least one level, which supports fencing policy
     */
    public static boolean isFencingPolicySupported(Set<Version> clusterLevels) {
        boolean supported = false;
        for (Version v : clusterLevels) {
            if (FeatureSupported.isSkipFencingIfSDActiveSupported(v)) {
                supported = true;
                break;
            }
        }
        return supported;
    }
}
