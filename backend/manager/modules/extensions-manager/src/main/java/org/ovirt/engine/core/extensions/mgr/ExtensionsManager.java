package org.ovirt.engine.core.extensions.mgr;

import static java.util.Arrays.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.ovirt.engine.api.extensionsold.Extension;
import org.ovirt.engine.api.extensionsold.Extension.ExtensionProperties;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for loading the required {@code Configuration} in order to create an extension. It holds
 * the logic of ordering and solving conflicts during loading the configuration
 */
public class ExtensionsManager {

    public static final String NAME = "ovirt.engine.extension.name";
    public static final String PROVIDES = "ovirt.engine.extension.provides";
    public static final String ENABLED = "ovirt.engine.extension.enabled";
    public static final String MODULE = "ovirt.engine.extension.module";
    public static final String CLASS = "ovirt.engine.extension.class";
    public static final String SENSITIVE_KEYS = "ovirt.engine.extension.sensitiveKeys";
    private static final String ENGINE_EXTENSION_ENABLED = "ENGINE_EXTENSION_ENABLED_";

    public static class ExtensionEntry {
        private File file;
        private boolean enabled;
        private boolean activated;
        private Extension extension;
        private Map<ExtensionProperties, Object> context = new EnumMap<>(ExtensionProperties.class);

        public ExtensionEntry(Properties props, File file) {
            this.file = file;
            load(props);
        }

        public String getName() {
            return (String) context.get(ExtensionProperties.NAME);
        }

        public File getFile() {
            return file;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isActivated() {
            return activated;
        }

        public List<String> getProvides() {
            List<String> providers = new ArrayList<>();
            for (String provider : ((String) context.get(ExtensionProperties.PROVIDES)).split(",")) {
                providers.add(provider.trim());
            }
            return providers;
        }

        public Map<ExtensionProperties, Object> getContext() {
            return context;
        }

        public Extension getExtension() {
            return extension;
        }

        public Properties getConfig() {
            return (Properties) context.get(ExtensionProperties.CONFIGURATION);
        }

        private void load(Properties props) {
            enabled = props.get(ENABLED) != null ? Boolean.parseBoolean(props.getProperty(ENABLED)) : true;
            context.put(ExtensionProperties.CONFIGURATION, props);
            context.put(ExtensionProperties.NAME, props.getProperty(NAME));
            context.put(ExtensionProperties.PROVIDES, props.getProperty(PROVIDES));
        }

    }

    private static final Logger log = LoggerFactory.getLogger(ExtensionsManager.class);
    private static volatile ExtensionsManager instance = null;
    private Map<String, ExtensionEntry> loadedEntries = new HashMap<>();
    private Map<String, Module> loadedModules = new HashMap<>();

    public static ExtensionsManager getInstance() {
        if (instance == null) {
            synchronized (ExtensionsManager.class) {
                if (instance == null) {
                    instance = new ExtensionsManager();
                }
            }
        }
        return instance;
    }

    public List<ExtensionEntry> getProvidedExtensions(String provides) {
        List<ExtensionEntry> results = new ArrayList<>();
        for (ExtensionEntry entry : loadedEntries.values()) {
            if (entry.activated && entry.getProvides().contains(provides)) {
                results.add(entry);
            }
        }
        return results;
    }

    public ExtensionEntry getExtensionByName(String pluginName) throws ConfigurationException {
        ExtensionEntry result = loadedEntries.get(pluginName);
        if (result == null) {
            throw new ConfigurationException(String.format(
                            "No configuration was found for extension named '%1$s'",
                            pluginName)
                    );

        }
        if (!result.activated) {
            throw new ConfigurationException(String.format(
                        "The configuration '%1$s' is not active",
                        pluginName)
                    );
        }
        return result;
    }

    private ExtensionsManager() {
        for (File directory : EngineLocalConfig.getInstance().getExtensionsDirectories()) {
            if (!directory.exists()) {
                log.warn(String.format("The directory '%1$s' cotaning configuration files does not exist.",
                        directory.getAbsolutePath()));
            } else {

                // The order of the files inside the directory is relevant, as the objects are created in the same order
                // that
                // the files are processed, so it is better to sort them so that objects will always be created in the
                // same
                // order regardless of how the filesystem decides to store the entries of the directory:
                File[] files = directory.listFiles();
                if (files != null) {
                    sort(files);
                    for (File file : files) {
                        if (file.getName().endsWith(".properties")) {
                            load(file);
                        }
                    }
                }
            }
        }
    }

    public void load(Properties configuration) {
        loadImpl(configuration, null);
    }

    public void load(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(inputStream);
            loadImpl(props, file);
        } catch (IOException exception) {
            throw new ConfigurationException(String.format("Can't load object configuration file '%1$s'",
                    file.getAbsolutePath()));
        }
    }

    private synchronized void loadImpl(Properties props, File confFile) {
        ExtensionEntry entry = new ExtensionEntry(props, confFile);
        ExtensionEntry alreadyLoadedEntry = loadedEntries.get(entry.getName());
        if (alreadyLoadedEntry != null) {
            throw new ConfigurationException(String.format(
                    "Could not load the configuration '%1$s' from file %2$s. A configuration with the same name was already loaded from file %3$s",
                    entry.getName(),
                    getFileName(entry.file),
                    getFileName(alreadyLoadedEntry.file))
             );
        }
        loadedEntries.put(entry.getName(), entry);
        entry.enabled =
                EngineLocalConfig.getInstance().getBoolean(ENGINE_EXTENSION_ENABLED + entry.getName(), entry.enabled);
        //Activate the extension
        if (entry.enabled && entry.extension == null) {
            try {
                entry.extension = (Extension) lookupService(
                        Extension.class,
                        entry.getConfig().getProperty(CLASS),
                        entry.getConfig().getProperty(MODULE)
                        ).newInstance();
                entry.extension.setContext(entry.context);
                entry.extension.init();
                entry.activated = true;
            } catch (Exception ex) {
                log.error(
                        String.format(
                                "Error in activating extension %1$s. Exception message is %2$s",
                                entry.getName(),
                                ex.getMessage()
                                )
                        );
                if (log.isDebugEnabled()) {
                    log.error("", ex);
                }
            }
        }
    }

    private String getFileName(File file) {
        return file != null ? file.getAbsolutePath() : "N/A";
    }

    private Module loadModule(String moduleSpec) {
        // If the module was not already loaded, load it
        try {
            Module module = loadedModules.get(moduleSpec);
            if (module == null) {
                ModuleLoader loader = ModuleLoader.forClass(this.getClass());
                if (loader == null) {
                    throw new ConfigurationException(String.format("The module '%1$s' cannot be loaded as the module system isn't enabled.",
                            moduleSpec));
                }
                module = loader.loadModule(ModuleIdentifier.fromString(moduleSpec));
                loadedModules.put(moduleSpec, module);
            }
            return module;
        } catch (ModuleLoadException exception) {
            throw new ConfigurationException(String.format("The module '%1$s' cannot be loaded.", moduleSpec),
                    exception);
        }
    }

    private Class<?> lookupService(Class<?> serviceInterface, String serviceClassName, String moduleName) {
        // Iterate over the service classes, and find the one that should
            // be instantiated and initialized.
        Module module = loadModule(moduleName);
        Class<?> serviceClass = null;
        for (Object service : ServiceLoader.load(serviceInterface, module.getClassLoader())) {
            if (service.getClass().getName().equals(serviceClassName)) {
                serviceClass = service.getClass();
                break;
            }
        }
        if (serviceClass == null) {
            throw new ConfigurationException(String.format("The module '%1$s' does not contain the service '%2$s'.",
                    module.getIdentifier().getName(),
                    serviceClassName));
        }
        return serviceClass;
    }

    public void dump() {
        log.info("Start of enabled extensions list");
        for (ExtensionEntry entry : loadedEntries.values()) {
            if (entry.extension != null) {
                Map<ExtensionProperties, Object> context = entry.extension.getContext();
                log.info(String.format(
                        "Instance name: '%1$s', Extension name: '%2$s', Version: '%3$s', License: '%4$s', Home: '%5$s', Author '%6$s',  File: '%7$s', Activated: '%8$s",
                        emptyIfNull(context.get(ExtensionProperties.NAME)),
                        emptyIfNull(context.get(ExtensionProperties.EXTENSION_NAME)),
                        emptyIfNull(context.get(ExtensionProperties.VERSION)),
                        emptyIfNull(context.get(ExtensionProperties.LICENSE)),
                        emptyIfNull(context.get(ExtensionProperties.HOME)),
                        emptyIfNull(context.get(ExtensionProperties.AUTHOR)),
                        emptyIfNull(getFileName(entry.file)),
                        entry.activated
                        )
                        );
            }
        }
        log.info("End of enabled extensions list");
    }

    private Object emptyIfNull(Object value) {
        return value == null ? "" : value;
    }
}
