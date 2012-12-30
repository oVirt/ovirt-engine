package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;

/**
 * Convenience class to check if a feature is supported or not in any given version.<br>
 * Methods should be named by feature and accept version to check against.
 */
public class FeatureSupported {

    private static boolean supportedInConfig(ConfigValues feature, Version version) {
        return Config.<Boolean> GetValue(feature, version.getValue());
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if network linking is supported for the version, <code>false</code> if it's not.
     */
    public static boolean networkLinking(Version version) {
        return supportedInConfig(ConfigValues.NetworkLinkingSupported, version);
    }
}
