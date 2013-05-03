package org.ovirt.engine.core.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * This class stores the local configuration (understanding local as the
 * configuration of the local machine, as opposed to the global configuration
 * stored in the database) of the engine loaded from the file specified by the
 * <code>ENGINE_VARS</code> environment variable.
 */
public class EngineLocalConfig extends LocalConfig {
    // The log:
    private static final Logger log = Logger.getLogger(LocalConfig.class);

    // Default files for defaults and overridden values:
    private static final String DEFAULTS_PATH = "/usr/share/ovirt-engine/conf/engine.conf.defaults";
    private static final String VARS_PATH = "/etc/ovirt-engine/engine.conf";

    // This is a singleton and this is the instance:
    private static final EngineLocalConfig instance = new EngineLocalConfig();

    public static EngineLocalConfig getInstance() {
        return instance;
    }

    private EngineLocalConfig() {
        // Locate the defaults file and add it to the list:
        String defaultsPath = System.getenv("ENGINE_DEFAULTS");
        if (defaultsPath == null) {
            defaultsPath = DEFAULTS_PATH;
        }

        // Locate the overridden values file and add it to the list:
        String varsPath = System.getenv("ENGINE_VARS");
        if (varsPath == null) {
            varsPath = VARS_PATH;
        }

        loadConfig(defaultsPath, varsPath);
    }

    public boolean isProxyEnabled() {
        return getBoolean("ENGINE_PROXY_ENABLED");
    }

    public int getProxyHttpPort() {
        return getInteger("ENGINE_PROXY_HTTP_PORT");
    }

    public int getProxyHttpsPort() {
        return getInteger("ENGINE_PROXY_HTTPS_PORT");
    }

    public String getHost() {
        return getProperty("ENGINE_FQDN");
    }

    public boolean isHttpEnabled() {
        return getBoolean("ENGINE_HTTP_ENABLED");
    }

    public int getHttpPort() {
        return getInteger("ENGINE_HTTP_PORT");
    }

    public boolean isHttpsEnabled() {
        return getBoolean("ENGINE_HTTPS_ENABLED");
    }

    public int getHttpsPort() {
        return getInteger("ENGINE_HTTPS_PORT");
    }

    public File getEtcDir() {
        return getFile("ENGINE_ETC");
    }

    public File getLogDir() {
        return getFile("ENGINE_LOG");
    }

    public File getTmpDir() {
        return getFile("ENGINE_TMP");
    }

    public File getUsrDir() {
        return getFile("ENGINE_USR");
    }

    public File getVarDir() {
        return getFile("ENGINE_VAR");
    }

    public File getCacheDir() {
        return getFile("ENGINE_CACHE");
    }

    /**
     * Gets the port number where the engine can be contacted using HTTP from
     * external hosts. This will usually be the proxy HTTP port if the proxy is
     * enabled or the engine HTTP port otherwise.
     */
    public int getExternalHttpPort () {
        return isProxyEnabled()? getProxyHttpPort(): getHttpPort();
    }

    /**
     * Gets the port number where the engine can be contacted using HTTPS from
     * external hosts. This will usually be the proxy HTTPS port if the proxy is
     * enabled or the engine HTTPS port otherwise.
     */
    public int getExternalHttpsPort () {
        return isProxyEnabled()? getProxyHttpsPort(): getHttpsPort();
    }

    /**
     * Gets the URL where the engine can be contacted using HTTP from external
     * hosts. This will usually be the proxy HTTP URL if the proxy is enabled or
     * the engine HTTP URL otherwise.
     *
     * @param path is the path that will be added after the address and port
     *     number of the URL
     */
    public URL getExternalHttpUrl(String path) throws MalformedURLException {
        return new URL("http", getHost(), getExternalHttpPort(), path);
    }

    /**
     * Gets the URL where the engine can be contacted using HTTPS from external
     * hosts. This will usually be the proxy HTTPS URL if the proxy is enabled or
     * the engine HTTPS URL otherwise.
     *
     * @param path is the path that will be added after the address and port
     *     number of the URL
     */
    public URL getExternalHttpsUrl(String path) throws MalformedURLException {
        return new URL("https", getHost(), getExternalHttpsPort(), path);
    }
}
