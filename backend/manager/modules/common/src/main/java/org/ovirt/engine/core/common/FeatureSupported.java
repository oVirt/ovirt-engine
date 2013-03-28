package org.ovirt.engine.core.common;

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

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if MTU specification is supported for the version, <code>false</code> if it's not.
     */
    public static boolean mtuSpecification(Version version) {
        return supportedInConfig(ConfigValues.MTUOverrideSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if non-VM network is supported for the version, <code>false</code> if it's not.
     */
    public static boolean nonVmNetwork(Version version) {
        return supportedInConfig(ConfigValues.NonVmNetworkSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if bridges element reported by VDSM is supported for the version, <code>false</code> if
     *         it's not.
     */
    public static boolean bridgesReportByVdsm(Version version) {
        return supportedInConfig(ConfigValues.SupportBridgesReportByVDSM, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if anti MAC spoofing is supported for the version, <code>false</code> if it's not.
     */
    public static boolean antiMacSpoofing(Version version) {
        return supportedInConfig(ConfigValues.EnableMACAntiSpoofingFilterRules, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if get hardware information is supported for the version, <code>false</code> if it's not.
     */
    public static boolean hardwareInfo(Version version) {
        return supportedInConfig(ConfigValues.HardwareInfoEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if tunnel migration is supported for the version, <code>false</code> if it's not.
     */
    public static boolean tunnelMigration(Version version) {
        return supportedInConfig(ConfigValues.TunnelMigrationEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if hot plug is supported for the version, <code>false</code> if it's not.
     */
    public static boolean hotPlug(Version version) {
        return supportedInConfig(ConfigValues.HotPlugEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if migration network is supported for the version, <code>false</code> if it's not.
     */
    public static boolean migrationNetwork(Version version) {
        return supportedInConfig(ConfigValues.MigrationNetworkEnabled, version);
    }
}
