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
     *
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if gluster services management feature is enabled, <code>false</code> if it's not.
     */
    public static boolean glusterServices(Version version) {
        return supportedInConfig(ConfigValues.GlusterServicesEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if gluster self-heal monitoring is supported, <code>false</code> if it's not.
     */
    public static boolean glusterSelfHealMonitoring(Version version) {
        return supportedInConfig(ConfigValues.GlusterSelfHealMonitoringSupported, version);
    }
}
