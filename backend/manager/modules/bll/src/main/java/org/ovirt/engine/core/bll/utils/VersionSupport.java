package org.ovirt.engine.core.bll.utils;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VersionSupport implements Serializable {

    private static final long serialVersionUID = 8368679686604395114L;
    private static Log log = LogFactory.getLog(VersionSupport.class);

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
        boolean isVersionSupported = !StringUtils.isEmpty(vds.getSupportedClusterLevels());
        if (isVersionSupported) {
            try {
                isVersionSupported = vds.getSupportedClusterVersionsSet().contains(clusterCompatibilityVersion);
            } catch (RuntimeException e) {
                log.errorFormat(e.getMessage());
                isVersionSupported = false;
            }
        }
        return isVersionSupported;
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @param action
     *            The action type to check for
     * @return <code>true</code> if the action is supported for the version, <code>false</code> if it's not.
     * @throws NullPointerException
     *             if an entry is missing from the action_version_map table for the queried action type
     */
    public static boolean isActionSupported(VdcActionType action, Version version) {
        Version minimalVersion =
                new Version(DbFacade.getInstance()
                        .getActionGroupDao()
                        .getActionVersionMapByActionType(action)
                        .getcluster_minimal_version());
        return version.compareTo(minimalVersion) >= 0;
    }
}
