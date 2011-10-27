package org.ovirt.engine.core.bll.utils;

import java.io.Serializable;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class VersionSupport implements Serializable {

    private VersionSupport() {

    }

    public static boolean checkVersionSupported(final Version compatibility_version) {
        boolean result = true;
        if (compatibility_version == null
                || !Config.<Set<Version>>GetValue(
                ConfigValues.SupportedClusterLevels).contains(compatibility_version)) {
            result = false;
        }
        return result;
    }

    public static String getUnsupportedVersionMessage() {
        return VdcBllMessages.ACTION_TYPE_FAILED_GIVEN_VERSION_NOT_SUPPORTED.toString();
    }

    /**
     * check that cluster version included in host supported clusters
     *
     * @param clusterCompatibilityVersion
     * @param vds
     * @return true if the version is supported, else false
     */
    public static boolean checkClusterVersionSupported(Version clusterCompatibilityVersion, VDS vds) {
        return (!StringHelper.isNullOrEmpty(vds.getsupported_cluster_levels())
                && vds.getSupportedClusterVersionsSet().contains(clusterCompatibilityVersion));
    }
}
