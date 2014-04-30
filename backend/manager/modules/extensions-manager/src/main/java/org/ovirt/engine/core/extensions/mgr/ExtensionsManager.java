package org.ovirt.engine.core.extensions.mgr;

import static java.util.Arrays.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.Extension;
/**
 * This class is responsible for loading the required {@code Configuration} in order to create an extension. It holds
 * the logic of ordering and solving conflicts during loading the configuration
 */
public class ExtensionsManager {
    private static final String ENGINE_EXTENSION_ENABLED = "ENGINE_EXTENSION_ENABLED_";

    public static final ExtKey TRACE_LOG_CONTEXT_KEY = new ExtKey("EXTENSION_MANAGER_TRACE_LOG", Logger.class, "863db666-3ea7-4751-9695-918a3197ad83");
    public static final ExtKey CAUSE_OUTPUT_KEY = new ExtKey("EXTENSION_MANAGER_CAUSE_OUTPUT_KEY", Throwable.class, "894e1c86-518b-40a2-a92b-29ea1eb0403d");

    private static interface BindingsLoader {
        Extension load(Properties props) throws Exception;
    }

    private static class JBossBindingsLoader implements BindingsLoader {
        private Map<String, Module> loadedModules = new HashMap<>();

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

        public Extension load(Properties props) throws Exception {
            return (Extension) lookupService(
                Extension.class,
                props.getProperty(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS),
                props.getProperty(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE)
            ).newInstance();
        }
    }

    private static class ExtensionEntry {

        private static int extensionNameIndex = 0;

        private String name;
        private File file;
        private boolean enabled;
        private boolean activated;
        private ExtensionProxy extension;

        private ExtensionEntry(Properties props, File file) {
            this.file = file;
            this.name = props.getProperty(
                Base.ConfigKeys.NAME,
                String.format("__unamed_%1$03d__", extensionNameIndex++)
            );
            this.enabled = Boolean.parseBoolean(props.getProperty(Base.ConfigKeys.ENABLED, "true"));
        }

        private String getFileName() {
            return file != null ? file.getAbsolutePath() : "N/A";
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ExtensionsManager.class);
    private static final Logger traceLog = LoggerFactory.getLogger(ExtensionsManager.class.getName() + ".trace");
    private static volatile ExtensionsManager instance = null;
    private Map<String, BindingsLoader> bindingsLoaders = new HashMap<>();
    private Map<String, ExtensionEntry> loadedEntries = new HashMap<>();
    private ExtMap globalContext = new ExtMap().mput(Base.GlobalContextKeys.EXTENSIONS, new ArrayList<ExtMap>());

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

    public List<ExtensionProxy> getProvidedExtensions(String provides) {
        List<ExtensionProxy> results = new ArrayList<>();
        for (ExtensionEntry entry : loadedEntries.values()) {
            if (entry.activated && entry.extension.getContext().<List>get(Base.ContextKeys.PROVIDES).contains(provides)) {
                results.add(entry.extension);
            }
        }
        return results;
    }

    public ExtensionProxy getExtensionByName(String name) throws ConfigurationException {
        ExtensionEntry entry = loadedEntries.get(name);
        ExtensionProxy result = null;
        if (entry != null && entry.activated) {
            result = entry.extension;
        }
        return result;
    }

    private ExtensionsManager() {

        bindingsLoaders.put(Base.ConfigBindingsMethods.JBOSSMODULE, new JBossBindingsLoader());

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

    private void dumpConfig(ExtensionProxy extension) {
        Logger logger = extension.getContext().<Logger>get(TRACE_LOG_CONTEXT_KEY);
        if (logger.isDebugEnabled()) {
            List sensitive = extension.getContext().<List>get(Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS);
            logger.debug("Config BEGIN");
            for (Map.Entry<Object, Object> entry : extension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).entrySet()) {
                logger.debug(
                    String.format(
                        "%s: %s",
                        entry.getKey(),
                        sensitive.contains(entry.getKey()) ? "***" : entry.getValue()
                    )
                );
            }
            logger.debug("Config END");
        }
    }

    private List<String> splitString(String s) {
        return new ArrayList<String>(Arrays.asList(s.trim().split("\\s*,\\s*", 0)));
    }

    private synchronized void loadImpl(Properties props, File confFile) {
        ExtensionEntry entry = new ExtensionEntry(props, confFile);
        ExtensionEntry alreadyLoadedEntry = loadedEntries.get(entry.name);
        if (alreadyLoadedEntry != null) {
            throw new ConfigurationException(String.format(
                    "Could not load the configuration '%1$s' from file %2$s. A configuration with the same name was already loaded from file %3$s",
                    entry.name,
                    entry.getFileName(),
                    alreadyLoadedEntry.getFileName()
             ));
        }
        loadedEntries.put(entry.name, entry);
        entry.enabled =
                EngineLocalConfig.getInstance().getBoolean(ENGINE_EXTENSION_ENABLED + entry.name, entry.enabled);
        //Activate the extension
        if (entry.enabled && entry.extension == null) {
            try {
                entry.extension = new ExtensionProxy(
                    loadExtension(props),
                    (
                        new ExtMap().mput(
                            Base.ContextKeys.GLOBAL_CONTEXT,
                            globalContext
                        ).mput(
                            TRACE_LOG_CONTEXT_KEY,
                            traceLog
                        ).mput(
                            Base.ContextKeys.INTERFACE_VERSION_MIN,
                            0
                        ).mput(
                            Base.ContextKeys.INTERFACE_VERSION_MAX,
                            Base.INTERFACE_VERSION_CURRENT
                        ).mput(
                            Base.ContextKeys.LOCALE,
                            Locale.getDefault().toString()
                        ).mput(
                            Base.ContextKeys.CONFIGURATION,
                            props
                        ).mput(
                            Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS,
                            splitString(props.getProperty(Base.ConfigKeys.SENSITIVE_KEYS, ""))
                        ).mput(
                            Base.ContextKeys.INSTANCE_NAME,
                            entry.name
                        ).mput(
                            Base.ContextKeys.PROVIDES,
                            splitString(props.getProperty(Base.ConfigKeys.PROVIDES, ""))
                        )
                    )
                );

                ExtMap output = entry.extension.invoke(
                    new ExtMap().mput(
                        Base.InvokeKeys.COMMAND,
                        Base.InvokeCommands.INITIALIZE
                    )
                );

                entry.extension.getContext().put(
                    TRACE_LOG_CONTEXT_KEY,
                    LoggerFactory.getLogger(
                        String.format(
                            "%1$s.%2$s.%3$s",
                            traceLog.getName(),
                            entry.extension.getContext().get(Base.ContextKeys.EXTENSION_NAME),
                            entry.extension.getContext().get(Base.ContextKeys.INSTANCE_NAME)
                        )
                    )
                );

                globalContext.<List<ExtMap>>get(Base.GlobalContextKeys.EXTENSIONS).add(
                    new ExtMap().mput(
                        Base.ExtensionRecord.INSTANCE_NAME,
                        entry.extension.getContext().get(Base.ContextKeys.INSTANCE_NAME)
                    ).mput(
                        Base.ExtensionRecord.PROVIDES,
                        entry.extension.getContext().get(Base.ContextKeys.PROVIDES)
                    ).mput(
                        Base.ExtensionRecord.EXTENSION,
                        entry.extension.getExtension()
                    ).mput(
                        Base.ExtensionRecord.CONTEXT,
                        entry.extension.getContext()
                    )
                );
                entry.activated = true;

                dumpConfig(entry.extension);
            } catch (Exception ex) {
                log.error(
                    String.format(
                        "Error in activating extension %1$s. Exception message is %2$s",
                        entry.name,
                        ex.getMessage()
                    )
                );
                if (log.isDebugEnabled()) {
                    log.error(ex.toString(), ex);
                }
            }
        }
    }

    private Extension loadExtension(Properties props) throws Exception {
        Extension extension;

        BindingsLoader loader = bindingsLoaders.get(props.getProperty(Base.ConfigKeys.BINDINGS_METHOD));
        if (loader == null) {
            throw new ConfigurationException(String.format("Invalid binding method '%1$s'.",
                    props.getProperty(Base.ConfigKeys.BINDINGS_METHOD)));
        }

        return loader.load(props);
    }

    public void dump() {
        log.info("Start of enabled extensions list");
        for (ExtensionEntry entry : loadedEntries.values()) {
            if (entry.extension != null) {
                ExtMap context = entry.extension.getContext();
                log.info(String.format(
                        "Instance name: '%1$s', Extension name: '%2$s', Version: '%3$s', Build interface Version: '%4$s', License: '%5$s', Home: '%6$s', Author '%7$s',  File: '%8$s', Activated: '%9$s",
                    emptyIfNull(context.get(Base.ContextKeys.INSTANCE_NAME)),
                    emptyIfNull(context.get(Base.ContextKeys.EXTENSION_NAME)),
                    emptyIfNull(context.get(Base.ContextKeys.VERSION)),
                    emptyIfNull(context.get(Base.ContextKeys.BUILD_INTERFACE_VERSION)),
                    emptyIfNull(context.get(Base.ContextKeys.LICENSE)),
                    emptyIfNull(context.get(Base.ContextKeys.HOME_URL)),
                    emptyIfNull(context.get(Base.ContextKeys.AUTHOR)),
                    entry.getFileName(),
                    entry.activated
                ));
            }
        }
        log.info("End of enabled extensions list");
    }

    private Object emptyIfNull(Object value) {
        return value == null ? "" : value;
    }
}
