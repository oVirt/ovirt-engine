package org.ovirt.engine.core.bll.utils;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionSupport implements Serializable {

    private static final long serialVersionUID = 8368679686604395114L;
    private static final Logger log = LoggerFactory.getLogger(VersionSupport.class);

    private VersionSupport() {
    }

    public static boolean checkVersionSupported(final Version compatibility_version) {
        boolean result = true;
        if (compatibility_version == null
                || !Config.<Set<Version>>getValue(
                ConfigValues.SupportedClusterLevels).contains(compatibility_version)) {
            result = false;
        }
        return result;
    }

    public static EngineMessage getUnsupportedVersionMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_GIVEN_VERSION_NOT_SUPPORTED;
    }

    /**
     * check that cluster version included in host supported clusters
     *
     * @return true if the version is supported, else false
     */
    public static boolean checkClusterVersionSupported(Version clusterCompatibilityVersion, VDS vds) {
        boolean isVersionSupported = !StringUtils.isEmpty(vds.getSupportedClusterLevels());
        if (isVersionSupported) {
            try {
                isVersionSupported = vds.getSupportedClusterVersionsSet().contains(clusterCompatibilityVersion);
            } catch (RuntimeException e) {
                log.error("{}", e.getMessage());
                log.debug("Exception", e);
                isVersionSupported = false;
            }
        }
        return isVersionSupported;
    }
}
