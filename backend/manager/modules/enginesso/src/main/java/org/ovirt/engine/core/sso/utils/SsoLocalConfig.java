package org.ovirt.engine.core.sso.utils;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;

import org.ovirt.engine.core.uutils.config.ShellLikeConfd;

@ApplicationScoped
public class SsoLocalConfig extends ShellLikeConfd {
    // Default files for defaults and overridden values:
    private static final String DEFAULTS_PATH = "/usr/share/ovirt-engine/conf/engine.conf.defaults";
    private static final String VARS_PATH = "/etc/ovirt-engine/engine.conf";

    public SsoLocalConfig() {
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

    public String getExtensionsPath() {
        return getProperty("ENGINE_EXTENSION_PATH");
    }

    public File getPKIEngineCert() {
        return getFile("ENGINE_PKI_ENGINE_CERT");
    }
}
