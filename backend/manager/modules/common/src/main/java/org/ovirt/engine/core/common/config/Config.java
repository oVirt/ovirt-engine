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
     * Fetch the CABaseDirectory configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for CABaseDirectory
     */
    public static String resolveCABasePath() {
        return ConfigUtil.resolvePath(Config.<String> GetValue(ConfigValues.ConfigDir),
                Config.<String> GetValue(ConfigValues.CABaseDirectory));
    }

    /**
     * Fetch the CACertificatePath configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for CACertificatePath
     */
    public static String resolveCACertificatePath() {
        return ConfigUtil.resolvePath(resolveCABasePath(), Config.<String> GetValue(ConfigValues.CACertificatePath));
    }

    /**
     * Fetch the CertificateFileName configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for CertificateFileName
     */
    public static String resolveCertificatePath() {
        return ConfigUtil.resolvePath(resolveCABasePath(), Config.<String> GetValue(ConfigValues.CertificateFileName));
    }

    /**
     * Fetch the SignScriptName configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for SignScriptName
     */
    public static String resolveSignScriptPath() {
        return ConfigUtil.resolvePath(resolveCABasePath(), Config.<String> GetValue(ConfigValues.SignScriptName));
    }

    /**
     * Fetch the keystoreUrl configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for keystoreUrl
     */
    public static String resolveKeyStorePath() {
        return ConfigUtil.resolvePath(resolveCABasePath(), Config.<String> GetValue(ConfigValues.keystoreUrl));
    }

    /**
     * Fetch the TruststoreUrl configuration value and, if it is not an absolute path, resolve it relative to the
     * CABaseDirectory configuration value.
     *
     * @return an absolute path for TruststoreUrl
     */
    public static String resolveTrustStorePath() {
        return ConfigUtil.resolvePath(resolveCABasePath(), Config.<String> GetValue(ConfigValues.TruststoreUrl));
    }

}
