package org.ovirt.engine.core.sso.service;

import static java.util.Arrays.sort;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.sso.utils.SsoLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoExtensionsManager extends ExtensionsManager {
    private static final String ENGINE_EXTENSION_ENABLED = "ENGINE_EXTENSION_ENABLED_";
    private static Logger log = LoggerFactory.getLogger(SsoExtensionsManager.class);
    private SsoLocalConfig localConfig;

    public SsoExtensionsManager(SsoLocalConfig localConfig) {
        super();
        this.localConfig = localConfig;
        getGlobalContext().put(
                Base.GlobalContextKeys.APPLICATION_NAME,
                Base.ApplicationNames.OVIRT_ENGINE);
        initialize();
    }

    private void initialize() {
        String path = localConfig.getExtensionsPath();
        List<File> extensionsDirectories = new ArrayList<>();
        if (path != null) {
            for (String currentPath : path.split(":")) {
                if (StringUtils.isNotBlank(currentPath)) {
                    extensionsDirectories.add(new File(currentPath));
                }
            }
        }

        for (File directory : extensionsDirectories) {
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
                                log.error("Could not load extension based on configuration file '{}'. " +
                                        "Please check the configuration file is valid. Exception message is: {}",
                                        file.getAbsolutePath(),
                                        ex.getMessage());
                                log.debug("Exception", ex);
                            }
                        }
                    }
                }
            }
        }

        for (ExtensionProxy extension : getLoadedExtensions()) {
            if (localConfig.getBoolean(
                    ENGINE_EXTENSION_ENABLED + normalizeName(
                            extension.getContext()
                                    .get(
                                            Base.ContextKeys.INSTANCE_NAME)),
                    Boolean.parseBoolean(
                            extension.getContext()
                                    .<Properties> get(
                                            Base.ContextKeys.CONFIGURATION)
                                    .getProperty(Base.ConfigKeys.ENABLED, "true")))) {
                try {
                    initialize(extension.getContext().get(Base.ContextKeys.INSTANCE_NAME));
                } catch (Exception ex) {
                    log.error("Could not initialize extension '{}'. Exception message is: {}",
                            extension.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME),
                            ex.getMessage());
                    log.debug("Exception", ex);
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
}
