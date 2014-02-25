package org.ovirt.engine.core.extensions.mgr;

import static java.util.Arrays.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
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
import org.ovirt.engine.core.extensions.mgr.Extension.ExtensionProperties;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for loading the required {@code Configuration} in order to create an extension. It holds
 * the logic of ordering and solving conflicts during loading the configuration
 */
public class ExtensionManager {

    private static final String NAME = "ovirt.engine.extension.name";
    private static final String PROVIDES = "ovirt.engine.extension.provides";
    private static final String ENABLED = "ovirt.engine.extension.enabled";
    private static final String MODULE = "ovirt.engine.extension.module";
    private static final String CLASS = "ovirt.engine.extension.class";
    private static final String ENGINE_EXTENSION_ENABLED = "ENGINE_EXTENSION_ENABLED_";

    public class ExtensionEntry {
        private File file;
        private boolean enabled;
        private Extension extension;
        private Map<Extension.ExtensionProperties, Object> context;

        public ExtensionEntry(File file) throws IOException {
            this.file = file;
            context = new EnumMap<>(ExtensionProperties.class);
            Properties props = new Properties();
            try (FileInputStream inputStream = new FileInputStream(file)) {
                enabled = props.get(ENABLED) != null ? Boolean.parseBoolean((String) props.get(ENABLED)) : true;
                props.load(inputStream);
                context.put(ExtensionProperties.CONFIGURATION, props);
                context.put(ExtensionProperties.NAME, props.getProperty(NAME));
                context.put(ExtensionProperties.PROVIDES, props.getProperty(PROVIDES));
            }
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

        public String getProvides() {
            return (String) context.get(ExtensionProperties.PROVIDES);
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
    }

    private static final Logger log = LoggerFactory.getLogger(ExtensionManager.class);
    private static volatile ExtensionManager instance = null;
    private Map<String, ExtensionEntry> loadedEntries = new HashMap<>();
    private Map<String, ExtensionEntry> activatedEntries = new HashMap<>();
    private Map<String, List<ExtensionEntry>> providesEntries = new HashMap<>();
    private Map<String, Module> loadedModules = new HashMap<>();

    public static ExtensionManager getInstance() {
        if (instance == null) {
            synchronized (ExtensionManager.class) {
                if (instance == null) {
                    instance = new ExtensionManager();
                }
            }
        }
        return instance;
    }

    public List<ExtensionEntry> getProvidedExtensions(String provides) {
        return providesEntries.containsKey(provides) ? providesEntries.get(provides)
                : Collections.<ExtensionEntry> emptyList();
    }

    public ExtensionEntry getExtensionByName(String pluginName) throws ConfigurationException {
        ExtensionEntry result = loadedEntries.get(pluginName);
        if (result == null) {
            throw new ConfigurationException("No configuration was found for extension named " + pluginName);
        }
        return result;
    }

    private ExtensionManager() {
        load();
    }

    private void load() throws ConfigurationException {
        for (File directory : EngineLocalConfig.getInstance().getExtensionsDirectories()) {
            load(directory);
        }
        activate();
    }

    private void load(File directory) throws ConfigurationException {
        // Check that the folder that contains the configuration files exists:
        if (!directory.exists()) {
            log.warn(String.format("The directory '%1$s' cotaning configuration files does not exist.",
                    directory.getAbsolutePath()));
        } else {

            // The order of the files inside the directory is relevant, as the objects are created in the same order
            // that
            // the files are processed, so it is better to sort them so that objects will always be created in the same
            // order regardless of how the filesystem decides to store the entries of the directory:
            File[] files = directory.listFiles();
            if (files != null) {
                sort(files);
                for (File file : files) {
                    if (file.getName().endsWith(".properties")) {
                        try {
                            ExtensionEntry entry =
                                    new ExtensionEntry(file);
                            ExtensionEntry alreadyLoded = loadedEntries.get(entry.getName());
                            if (alreadyLoded != null) {
                                throw new ConfigurationException(String.format("Could not load the configuration file '%1$s'. The configuration file '%2$s' already has the name '%3$s'",
                                        file.getAbsolutePath(),
                                        alreadyLoded.file.getAbsolutePath(),
                                        entry.getName()));
                            }
                            loadedEntries.put(entry.getName(), entry);

                        } catch (IOException exception) {
                            throw new ConfigurationException(String.format("Can't load object configuration file '%1$s'",
                                    file.getAbsolutePath()));
                        }
                    }
                }
            }
        }
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

    /**
     * Activates the enabled configurations
     */
    private void activate() {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        for (ExtensionEntry entry : loadedEntries.values()) {
            //Engine local config might override the enabled property of the configuration
            // if a proper entry exists at the engine config.
            entry.enabled = config.getBoolean(ENGINE_EXTENSION_ENABLED + entry.getName(), entry.enabled);
            if (entry.enabled) {
                try {
                    entry.extension = (Extension) lookupService(
                            Extension.class,
                            entry.getConfig().getProperty(CLASS),
                            entry.getConfig().getProperty(MODULE)
                    ).newInstance();
                    entry.extension.setContext(entry.context);
                    entry.extension.init();
                    activatedEntries.put(entry.getName(), entry);
                    MultiValueMapUtils.addToMap(entry.getProvides(), entry, providesEntries);
                } catch (Exception ex) {
                    entry.enabled = false;
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
    }
}
