package org.ovirt.engine.core.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.uutils.config.ShellLikeConfd;

/**
 * This class stores the local configuration (understanding local as the
 * configuration of the local machine, as opposed to the global configuration
 * stored in the database) of the engine loaded from the file specified by the
 * <code>ENGINE_VARS</code> environment variable.
 */
public class EngineLocalConfig extends ShellLikeConfd {

    // Default files for defaults and overridden values:
    private static final String DEFAULTS_PATH = "/usr/share/ovirt-engine/conf/engine.conf.defaults";
    private static final String VARS_PATH = "/etc/ovirt-engine/engine.conf";

    // This is a singleton and this is the instance:
    private static volatile EngineLocalConfig instance;

    public static EngineLocalConfig getInstance() {
        return getInstance(null);
    }

    public static EngineLocalConfig getInstance(Map<String, String> values) {
        if (values != null) {
            instance = new EngineLocalConfig(values);
        } else {
            if (instance == null) {
                synchronized(EngineLocalConfig.class) {
                    if (instance == null) {
                        instance = new EngineLocalConfig();
                    }
                }
            }
        }
        return instance;
    }

    public static void clearInstance() {
        synchronized(EngineLocalConfig.class) {
            instance = null;
        }
    }

    protected EngineLocalConfig(Map<String, String> values) {
        setConfig(values);
    }

    private EngineLocalConfig() {
        String v;

        String defaultsPath = DEFAULTS_PATH;
        v = System.getProperty("ovirt-engine.config.defaults");
        if (v != null) {
            defaultsPath = v;
        }
        v = System.getenv("ENGINE_DEFAULTS");
        if (v != null) {
            defaultsPath = v;
        }

        String varsPath = VARS_PATH;
        v = System.getProperty("ovirt-engine.config.vars");
        if (v != null) {
            varsPath = v;
        }
        v = System.getenv("ENGINE_VARS");
        if (v != null) {
            varsPath = v;
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

    public File getExternalProvidersTrustStore() {
        return getFile("ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE");
    }

    public String getExternalProvidersTrustStoreType() {
        return getProperty("ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE_TYPE");
    }

    public String getExternalProvidersTrustStorePassword() {
        return getProperty("ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE_PASSWORD");
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
        return new URL("http", getHost(), getExternalHttpPort(), getEngineURI() + path);
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
        return getExternalHttpsBaseUrl(getEngineURI() + path);
    }

    public URL getExternalHttpsBaseUrl(String path) throws MalformedURLException {
        return new URL("https", getHost(), getExternalHttpsPort(), path);
    }

    public File getPKIDir() {
        return getFile("ENGINE_PKI");
    }

    public File getPKICACert() {
        return getFile("ENGINE_PKI_CA");
    }

    public File getPKIQemuCACert() {
        return getFile("ENGINE_PKI_QEMU_CA");
    }

    public File getPKIEngineCert() {
        return getFile("ENGINE_PKI_ENGINE_CERT");
    }

    public String getPKITrustStoreType() {
        return getProperty("ENGINE_PKI_TRUST_STORE_TYPE");
    }

    public File getPKITrustStore() {
        return getFile("ENGINE_PKI_TRUST_STORE");
    }

    public String getPKITrustStorePath() {
        return getProperty("ENGINE_PKI_TRUST_STORE");
    }

    public String getPKITrustStorePassword() {
        return getProperty("ENGINE_PKI_TRUST_STORE_PASSWORD");
    }

    public String getHttpsPKITrustStoreType() {
        return getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_TYPE");
    }

    public File getHttpsPKITrustStore() {
        return getFile("ENGINE_HTTPS_PKI_TRUST_STORE");
    }

    public String getHttpsPKITrustStorePath() {
        return getProperty("ENGINE_HTTPS_PKI_TRUST_STORE");
    }

    public String getHttpsPKITrustStorePassword() {
        return getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_PASSWORD");
    }

    public String getPKIEngineStoreType() {
        return getProperty("ENGINE_PKI_ENGINE_STORE_TYPE");
    }

    public File getPKIEngineStore() {
        return getFile("ENGINE_PKI_ENGINE_STORE");
    }

    public String getPKIEngineStorePassword() {
        return getProperty("ENGINE_PKI_ENGINE_STORE_PASSWORD");
    }

    public String getPKIEngineStoreAlias() {
        return getProperty("ENGINE_PKI_ENGINE_STORE_ALIAS");
    }

    public File getEngineUpMark(){
        return getFile("ENGINE_UP_MARK");
    }

    public String getEngineURI(){
        return getProperty("ENGINE_URI");
    }

    public String getSsoStoreEku() {
        return getProperty("ENGINE_SSO_ENGINE_STORE_EKU");
    }

    public String getEngineGrafanaFqdn() {
        return getProperty("ENGINE_GRAFANA_FQDN");
    }

    public String getEngineGrafanaBaseUrl() {
        return getProperty("ENGINE_GRAFANA_BASE_URL");
    }

    /**
     * Returns the directory for custom/3rd party extension configuration files
     */
    public List<File> getExtensionsDirectories() {
        String path = getProperty("ENGINE_EXTENSION_PATH", true);
        if (path == null) {
            return Collections.emptyList();
        }
        List<File> results = new ArrayList<>();
        for (String currentPath : path.split(":")) {
            if (StringUtils.isNotBlank(currentPath)) {
                results.add(new File(currentPath));
            }

        }
        return results;
    }
}
