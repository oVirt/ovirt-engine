package org.ovirt.engine.core.utils.extensionsmgr;

import static java.util.Arrays.sort;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineExtensionsManager extends ExtensionsManager {

    private static final String ENGINE_EXTENSION_ENABLED = "ENGINE_EXTENSION_ENABLED_";
    // The pattern of extension types to ignore
    private static final String ENGINE_EXTENSIONS_IGNORED = "ENGINE_EXTENSIONS_IGNORED";

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

        Pattern pattern = Pattern.compile(EngineLocalConfig.getInstance()
                .getProperty(ENGINE_EXTENSIONS_IGNORED));

        for (ExtensionProxy extension : getLoadedExtensions()) {
            if (
                EngineLocalConfig.getInstance().getBoolean(
                    ENGINE_EXTENSION_ENABLED + normalizeName(
                        extension.getContext().get(
                            Base.ContextKeys.INSTANCE_NAME
                        )
                    ),
                    Boolean.parseBoolean(
                            extension.getContext().<Properties> get(
                                    Base.ContextKeys.CONFIGURATION
                            ).getProperty(Base.ConfigKeys.ENABLED, "true")
                    )
                ) &&
                extension.getContext().<Collection<String>> get(Base.ContextKeys.PROVIDES).stream()
                        .noneMatch(p -> pattern.matcher(p).matches())
            ) {
                try {
                    initialize(extension.getContext().get(Base.ContextKeys.INSTANCE_NAME));
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
        StringBuilder ret = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                ret.append(c);
            } else {
                ret.append('_');
            }
        }
        return ret.toString();
    }

}
