package org.ovirt.engine.core.common.gluster;

import static org.ovirt.engine.core.common.FeatureSupported.supportedInConfig;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;

/**
 * Convenience class to check if a gluster feature is supported or not in any given version.<br>
 * Methods should be named by feature and accept version to check against.
 */
public class GlusterFeatureSupported {
    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if gluster support is enabled, <code>false</code> if it's not.
     */
    public static boolean gluster(Version version) {
        return supportedInConfig(ConfigValues.GlusterSupport, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if gluster heavyweight refresh is enabled, <code>false</code> if it's not.
     */
    public static boolean refreshHeavyWeight(Version version) {
        return supportedInConfig(ConfigValues.GlusterRefreshHeavyWeight, version);
    }

}
