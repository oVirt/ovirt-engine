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

    /**
     *
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if gluster hooks management feature is enabled, <code>false</code> if it's not.
     */
    public static boolean glusterHooks(Version version) {
        return supportedInConfig(ConfigValues.GlusterHooksEnabled, version);
    }

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
     * @return <code>true</code> if gluster host UUID is supported, <code>false</code> if it's not.
     */
    public static boolean glusterHostUuidSupported(Version version) {
        return supportedInConfig(ConfigValues.GlusterHostUUIDSupport, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if there's support for gluster task monitoring (rebalance, remove-brick),
     *         <code>false</code> if it's not.
     */
    public static boolean glusterAsyncTasks(Version version) {
        return supportedInConfig(ConfigValues.GlusterAsyncTasksSupport, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if there's support for force option in Create Volume, <code>false</code> if it's not.
     */
    public static boolean glusterForceCreateVolumeSupported(Version version) {
        if (version != null) {
            return supportedInConfig(ConfigValues.GlusterSupportForceCreateVolume, version);
        }
        else {
            return false;
        }
    }

    /**
    *
    * @param version
    *            Compatibility version to check for.
    * @return <code>true</code> if gluster geo-replication management feature is enabled,
    *         <code>false</code> if it's not.
    */
   public static boolean glusterGeoReplication(Version version) {
       return supportedInConfig(ConfigValues.GlusterGeoReplicationEnabled, version);
   }
}
