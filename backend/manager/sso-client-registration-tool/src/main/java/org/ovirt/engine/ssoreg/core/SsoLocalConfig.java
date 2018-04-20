package org.ovirt.engine.ssoreg.core;

import java.io.File;
import java.util.Map;

import org.ovirt.engine.core.uutils.config.ShellLikeConfd;

/**
 * This class stores the local configuration (understanding local as the
 * configuration of the local machine, as opposed to the global configuration
 * stored in the database) of the engine loaded from the file specified by the
 * <code>ENGINE_VARS</code> environment variable.
 */
public class SsoLocalConfig extends ShellLikeConfd {

    // Default files for defaults and overridden values:
    private static final String DEFAULTS_PATH = "/usr/share/ovirt-engine/conf/engine.conf.defaults";
    private static final String VARS_PATH = "/etc/ovirt-engine/engine.conf";

    // This is a singleton and this is the instance:
    private static volatile SsoLocalConfig instance;

    public static SsoLocalConfig getInstance() {
        return getInstance(null);
    }

    public static SsoLocalConfig getInstance(Map<String, String> values) {
        if (values != null) {
            instance = new SsoLocalConfig(values);
        } else {
            if (instance == null) {
                synchronized(SsoLocalConfig.class) {
                    if (instance == null) {
                        instance = new SsoLocalConfig();
                    }
                }
            }
        }
        return instance;
    }

    protected SsoLocalConfig(Map<String, String> values) {
        setConfig(values);
    }

    private SsoLocalConfig() {
        String v;

        String defaultsPath = System.getProperty("ovirt-engine.config.defaults", DEFAULTS_PATH);
        v = System.getenv("ENGINE_DEFAULTS");
        if (v != null) {
            defaultsPath = v;
        }

        String varsPath = System.getProperty("ovirt-engine.config.vars", VARS_PATH);
        v = System.getenv("ENGINE_VARS");
        if (v != null) {
            varsPath = v;
        }

        loadConfig(defaultsPath, varsPath);
    }

    public File getLogDir() {
        return getFile("ENGINE_LOG");
    }

    public File getTmpDir() {
        return getFile("ENGINE_TMP");
    }

}
