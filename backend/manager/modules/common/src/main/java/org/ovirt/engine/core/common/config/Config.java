package org.ovirt.engine.core.common.config;

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

    public static <T> T GetValue(ConfigValues value) {
        return Config.<T> GetValue(value, ConfigCommon.defaultConfigurationVersion);
    }

    public static <T> T GetValue(ConfigValues value, String version) {
        return getConfigUtils().<T> GetValue(value, version);
    }

    /**
     * Fetch the oVirtISOsRepositoryPath configuration value and, if it is not an absolute path, resolve it relative to
     * the DataDir configuration value.
     *
     * @return an absolute path for oVirtISOsRepositoryPath
     */
    public static String resolveOVirtISOsRepositoryPath() {
        return ConfigUtil.resolvePath(Config.<String> GetValue(ConfigValues.DataDir),
                Config.<String> GetValue(ConfigValues.oVirtISOsRepositoryPath));
    }

    /**
     * Fetch the AttestationTruststoreUrl configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for AttestaionTruststore
     */
    public static String resolveAttestationTrustStorePath() {
        return ConfigUtil.resolvePath(Config.<String> GetValue(ConfigValues.DataDir),
                Config.<String> GetValue(ConfigValues.AttestationTruststore));
    }

}
