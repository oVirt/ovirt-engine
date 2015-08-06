package org.ovirt.engine.core.utils.extensionsmgr;

import static java.util.Arrays.sort;

import java.io.File;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.crypto.EnvelopePBE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineExtensionsManager extends ExtensionsManager {

    private static final String ENGINE_EXTENSION_ENABLED = "ENGINE_EXTENSION_ENABLED_";

    private static volatile EngineExtensionsManager instance = null;
    private static Logger log = LoggerFactory.getLogger(EngineExtensionsManager.class);

    public static EngineExtensionsManager getInstance() {
        if (instance == null) {
            synchronized (EngineExtensionsManager.class) {
                if (instance == null) {
                    instance = new EngineExtensionsManager();
                }
            }
        }
        return instance;
    }

    public EngineExtensionsManager() {
        super();
        getGlobalContext().put(
                Base.GlobalContextKeys.APPLICATION_NAME,
                Base.ApplicationNames.OVIRT_ENGINE);
    }

    public void engineInitialize() {
        try {
            createInternalAAAConfigurations();
        } catch (Exception ex) {
            log.error("Could not load built in configuration. Exception message is: {}",
                    ex.getMessage());
            log.debug("", ex);
        }

        for (File directory : EngineLocalConfig.getInstance().getExtensionsDirectories()) {
            if (!directory.exists()) {
                log.warn("The directory '{}' cotaning configuration files does not exist.",
                        directory.getAbsolutePath());
            } else {

                // The order of the files inside the directory is relevant, as the objects are created in
                // the same order
                // that
                // the files are processed, so it is better to sort them so that objects will always be
                // created in the
                // same
                // order regardless of how the filesystem decides to store the entries of the directory:
                File[] files = directory.listFiles();
                if (files != null) {
                    sort(files);
                    for (File file : files) {
                        if (file.getName().endsWith(".properties")) {
                            try {
                                load(file);
                            } catch (Exception ex) {
                                log.error("Could not load extension based on configuration file '{}'. Please check the configuration file is valid. Exception message is: {}",
                                        file.getAbsolutePath(),
                                        ex.getMessage());
                                log.debug("", ex);
                            }
                        }
                    }
                }
            }
        }

        for (ExtensionProxy extension : getLoadedExtensions()) {
            if (
                EngineLocalConfig.getInstance().getBoolean(
                    ENGINE_EXTENSION_ENABLED + normalizeName(
                        extension.getContext().<String> get(
                            Base.ContextKeys.INSTANCE_NAME
                        )
                    ),
                    Boolean.parseBoolean(
                            extension.getContext().<Properties> get(
                                    Base.ContextKeys.CONFIGURATION
                            ).getProperty(Base.ConfigKeys.ENABLED, "true")
                    )
                )
            ) {
                try {
                    initialize(extension.getContext().<String>get(Base.ContextKeys.INSTANCE_NAME));
                } catch (Exception ex) {
                    log.error("Could not initialize extension '{}'. Exception message is: {}",
                            extension.getContext().<String>get(Base.ContextKeys.INSTANCE_NAME),
                            ex.getMessage());
                    log.debug("", ex);
                }
            }
        }

        dump();
    }

    private String normalizeName(String s) {
        StringBuilder ret = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '_' || Character.isLetterOrDigit(c)) {
                ret.append(c);
            } else {
                ret.append('_');
            }
        }
        return ret.toString();
    }

    private void createInternalAAAConfigurations() {
        try {
            Properties authConfig = new Properties();
            authConfig.put(Base.ConfigKeys.NAME, "builtin-authn-internal");
            authConfig.put(Base.ConfigKeys.PROVIDES, Authn.class.getName());
            authConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
            authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
            authConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS,
                    "org.ovirt.engine.extensions.aaa.builtin.internal.InternalAuthn");
            authConfig.put("ovirt.engine.aaa.authn.profile.name", "internal");
            authConfig.put("ovirt.engine.aaa.authn.authz.plugin", "internal");
            authConfig.put("config.authn.user.name", Config.<String> getValue(ConfigValues.AdminUser));
            authConfig.put(
                "config.authn.user.password",
                EnvelopePBE.encode(
                    "PBKDF2WithHmacSHA1",
                    256,
                    4000,
                    null,
                    Config.<String> getValue(ConfigValues.AdminPassword)
                )
            );
            authConfig.put(Base.ConfigKeys.SENSITIVE_KEYS, "config.authn.user.password");

            load(authConfig);
        } catch (Exception ex) {
            log.error("Could not load auth config internal aaa extension based on configuration. Exception message is: {}",
                    ex.getMessage());
            log.debug("", ex);
        }

        Properties dirConfig = new Properties();
        dirConfig.put(Base.ConfigKeys.NAME, "internal");
        dirConfig.put(Base.ConfigKeys.PROVIDES, Authz.class.getName());
        dirConfig.put(Base.ConfigKeys.BINDINGS_METHOD, Base.ConfigBindingsMethods.JBOSSMODULE);
        dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE, "org.ovirt.engine.extensions.builtin");
        dirConfig.put(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS,
                "org.ovirt.engine.extensions.aaa.builtin.internal.InternalAuthz");
        dirConfig.put("config.authz.user.name", Config.<String> getValue(ConfigValues.AdminUser));
        dirConfig.put("config.query.filter.size", "10");
        try {
            load(dirConfig);
        } catch (Exception ex) {
            log.error("Could not load directory config internal aaa extension based on configuration. Exception message is: {}",
                    ex.getMessage());
            log.debug("", ex);
        }
    }

}
