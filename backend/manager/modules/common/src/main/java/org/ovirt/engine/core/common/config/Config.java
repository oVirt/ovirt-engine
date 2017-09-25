package org.ovirt.engine.core.common.config;

import java.util.Map;

/**
 * Config Class
 */
public final class Config {
    private static IConfigUtilsInterface _configUtils;

    public static IConfigUtilsInterface getConfigUtils() {
        return _configUtils;
    }

    public static void setConfigUtils(IConfigUtilsInterface value) {
        _configUtils = value;
    }

    public static <T> T getValue(ConfigValues value) {
        return Config.getValue(value, ConfigCommon.defaultConfigurationVersion);
    }

    public static <T> Map<String, T> getValuesForAllVersions(ConfigValues value) {
        return getConfigUtils().getValuesForAllVersions(value);
    }

    public static <T> T getValue(ConfigValues value, String version) {
        return getConfigUtils().getValue(value, version);
    }

    public static boolean valueExists(ConfigValues configValue, String version) {
        return getConfigUtils().valueExists(configValue, version);
    }

    public static void refresh() {
        getConfigUtils().refresh();
    }

    /**
     * Fetch the oVirtISOsRepositoryPath configuration value and, if it is not an absolute path, resolve it relative to
     * the DataDir configuration value.
     *
     * @return an absolute path for oVirtISOsRepositoryPath
     */
    public static String resolveOVirtISOsRepositoryPath() {
        return ConfigUtil.resolvePath(Config.getValue(ConfigValues.DataDir),
                Config.getValue(ConfigValues.oVirtISOsRepositoryPath));
    }

    /**
     * Fetch the AttestationTruststoreUrl configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for AttestaionTruststore
     */
    public static String resolveAttestationTrustStorePath() {
        return ConfigUtil.resolvePath(Config.getValue(ConfigValues.DataDir),
                Config.getValue(ConfigValues.AttestationTruststore));
    }
}
