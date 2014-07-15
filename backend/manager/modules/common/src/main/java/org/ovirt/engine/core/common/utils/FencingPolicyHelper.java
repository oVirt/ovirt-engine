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
        // Version 2.2 is not supported by VDSM, but it's still contained in version list,
        // so we cannot just get first element from version list
        Version ver = Version.v3_0;
        if (fp != null) {
            if (fp.isSkipFencingIfSDActive()) {
                for (Version v : Version.ALL) {
                    // Version 2.2 is included in version list, but it's not included in db to set up config values
                    if (v.compareTo(Version.v3_0) >= 0 &&
                            FeatureSupported.isSkipFencingIfSDActiveSupported(v)) {
                        ver = v;
                        break;
                    }
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
