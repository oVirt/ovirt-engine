package org.ovirt.engine.core.extensions.mgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for loading the required {@code Configuration} in order to create an extension. It holds
 * the logic of ordering and solving conflicts during loading the configuration
 */
public class ExtensionsManager extends Observable {

    private static final Logger log = LoggerFactory.getLogger(ExtensionsManager.class);
    private static final Logger traceLog = LoggerFactory.getLogger(ExtensionsManager.class.getName() + ".trace");

    public static final ExtKey TRACE_LOG_CONTEXT_KEY = new ExtKey("EXTENSION_MANAGER_TRACE_LOG",
            Logger.class,
            "863db666-3ea7-4751-9695-918a3197ad83");
    public static final ExtKey CAUSE_OUTPUT_KEY = new ExtKey("EXTENSION_MANAGER_CAUSE_OUTPUT_KEY", Throwable.class, "894e1c86-518b-40a2-a92b-29ea1eb0403d");

    private static interface BindingsLoader {
        ExtensionProxy load(Properties props) throws Exception;
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
                throw new ConfigurationException(String.format("The module '%1$s' cannot be loaded: %2$s", moduleSpec, exception.getMessage()),
                        exception);
            }
        }

        private <T extends Class> T lookupService(Module module, T serviceInterface, String serviceClassName) {
            T serviceClass = null;
            for (Object service : module.loadService(serviceInterface)) {
                if (service.getClass().getName().equals(serviceClassName)) {
                    serviceClass = (T)service.getClass();
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

        public ExtensionProxy load(Properties props) throws Exception {
            Module module = loadModule(
                props.getProperty(Base.ConfigKeys.BINDINGS_JBOSSMODULE_MODULE)
            );

            return new ExtensionProxy(
                module.getClassLoader(),
                lookupService(
                    module,
                    Extension.class,
                    props.getProperty(Base.ConfigKeys.BINDINGS_JBOSSMODULE_CLASS)
                ).newInstance()
            );
        }
    }

    private static class ExtensionEntry {

        private static int extensionNameIndex = 0;

        private String name;
        private File file;
        private boolean enabled;
        private boolean initialized;
        private ExtensionProxy extension;

        private ExtensionEntry(Properties props, File file) {
            this.file = file;
            this.name = props.getProperty(
                Base.ConfigKeys.NAME,
                String.format("__unamed_%1$03d__", extensionNameIndex++)
            );
            this.enabled = Boolean.valueOf(props.getProperty(Base.ConfigKeys.ENABLED, "true"));
        }

        private String getFileName() {
            return file != null ? file.getAbsolutePath() : "N/A";
        }
    }

    private static final Map<String, BindingsLoader> bindingsLoaders = new HashMap<>();
    static {
        bindingsLoaders.put(Base.ConfigBindingsMethods.JBOSSMODULE, new JBossBindingsLoader());
    }

    private ConcurrentMap<String, ExtensionEntry> loadedEntries = new ConcurrentHashMap<>();
    private ConcurrentMap<String, ExtensionEntry> initializedEntries = new ConcurrentHashMap<>();
    private final ExtMap globalContext = new ExtMap().mput(Base.GlobalContextKeys.EXTENSIONS, new ArrayList<ExtMap>());


    public String load(Properties configuration) {
        return loadImpl(configuration, null);
    }

    public String load(File file) {
        try (
            InputStream is = new FileInputStream(file);
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)
        ) {
            Properties props = new Properties();
            props.load(reader);
            return loadImpl(props, file);
        } catch (IOException exception) {
            throw new ConfigurationException(String.format("Can't load object configuration file '%1$s': %2$s",
                    file.getAbsolutePath(), exception.getMessage()), exception);
        }
    }

    private void dumpConfig(ExtensionProxy extension) {
        Logger traceLogger = extension.getContext().get(TRACE_LOG_CONTEXT_KEY);
        if (traceLogger.isDebugEnabled()) {
            Collection sensitive = extension.getContext().get(Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS);
            traceLogger.debug("Config BEGIN");
            for (Map.Entry<Object, Object> entry : extension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).entrySet()) {
                traceLogger.debug(
                    String.format(
                        "%s: %s",
                        entry.getKey(),
                        sensitive.contains(entry.getKey()) ? "***" : entry.getValue()
                    )
                );
            }
            traceLogger.debug("Config END");
        }
    }

    private Collection<String> splitString(String s) {
        return new ArrayList<>(Arrays.asList(s.trim().split("\\s*,\\s*", 0)));
    }

    private synchronized String loadImpl(Properties props, File confFile) {
        ExtensionEntry entry = new ExtensionEntry(props, confFile);
        if (!entry.enabled) {
            return null;
        }

        ExtensionEntry alreadyLoadedEntry = loadedEntries.get(entry.name);
        if (alreadyLoadedEntry != null) {
            throw new ConfigurationException(String.format(
                    "Could not load the configuration '%1$s' from file %2$s. A configuration with the same name was already loaded from file %3$s",
                    entry.name,
                    entry.getFileName(),
                    alreadyLoadedEntry.getFileName()
             ));
        }
        try {
            entry.extension = loadExtension(props);
            entry.extension.getContext().mput(
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
                            Base.ContextKeys.CONFIGURATION_FILE,
                            entry.file == null ? null : entry.file.getAbsolutePath()
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
                    );

            log.info("Loading extension '{}'", entry.name);
            ExtMap output = entry.extension.invoke(
                    new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Base.InvokeCommands.LOAD
                            )
                    );
            log.info("Extension '{}' loaded", entry.name);

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
           if (output.<Integer>get(Base.InvokeKeys.RESULT) != Base.InvokeResult.SUCCESS) {
               throw new RuntimeException(
                       String.format("Invoke of LOAD returned with error code: %1$s",
                       output.<Integer>get(Base.InvokeKeys.RESULT)
                       )
               );
           }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error loading extension '%1$s': %2$s", entry.name, e.getMessage()), e);
        }
        loadedEntries.put(entry.name, entry);
        dumpConfig(entry.extension);
        setChanged();
        notifyObservers();
        return entry.name;
    }

    public ExtMap getGlobalContext() {
        return globalContext;
    }

    public List<ExtensionProxy> getExtensionsByService(String provides) {
        List<ExtensionProxy> results = new ArrayList<>();
        for (ExtensionEntry entry : initializedEntries.values()) {
            if (entry.extension.getContext().<Collection<String>> get(Base.ContextKeys.PROVIDES).contains(provides)) {
                results.add(entry.extension);
            }
        }
        return results;
    }

    public ExtensionProxy getExtensionByName(String name) throws ConfigurationException {
        if (name == null) {
            throw new ConfigurationException("Extension was not specified");
        }
        ExtensionEntry entry = initializedEntries.get(name);
        if (entry == null) {
            throw new ConfigurationException(String.format("Extension %1$s could not be found", name));
        }
        return entry.extension;
    }

    public List<ExtensionProxy> getLoadedExtensions() {
        List<ExtensionProxy> results = new ArrayList<>(loadedEntries.size());
        for (ExtensionEntry entry : loadedEntries.values()) {
            results.add(entry.extension);
        }
        return results;
    }

    public List<ExtensionProxy> getExtensions() {
        List<ExtensionProxy> results = new ArrayList<>(initializedEntries.size());
        for (ExtensionEntry entry : initializedEntries.values()) {
            results.add(entry.extension);
        }
        return results;
    }

    public ExtensionProxy initialize(String extensionName) {
        ExtensionEntry entry = loadedEntries.get(extensionName);
        if (entry == null) {
            throw new RuntimeException(String.format("No extensioned with instance name %1$s could be found",
                    extensionName));
        }
        try {
            log.info("Initializing extension '{}'", entry.name);
            entry.extension.invoke(new ExtMap().mput(Base.InvokeKeys.COMMAND, Base.InvokeCommands.INITIALIZE));
            log.info("Extension '{}' initialized", entry.name);
        } catch (Exception ex) {
            log.error("Error in activating extension '{}': {}", entry.name, ex.getMessage());
            if (log.isDebugEnabled()) {
                log.debug(ex.toString(), ex);
            }
            throw new RuntimeException(ex);
        }
        entry.initialized = true;
        initializedEntries.put(extensionName, entry);
        synchronized (globalContext) {
            globalContext.<Collection<ExtMap>> get(Base.GlobalContextKeys.EXTENSIONS).add(
                    new ExtMap().mput(
                            Base.ExtensionRecord.INSTANCE_NAME,
                            entry.extension.getContext().get(Base.ContextKeys.INSTANCE_NAME)
                            ).mput(
                                    Base.ExtensionRecord.PROVIDES,
                                    entry.extension.getContext().get(Base.ContextKeys.PROVIDES)
                            ).mput(
                                    Base.ExtensionRecord.CLASS_LOADER,
                                    entry.extension.getClassLoader()
                            ).mput(
                                    Base.ExtensionRecord.EXTENSION,
                                    entry.extension.getExtension()
                            ).mput(
                                    Base.ExtensionRecord.CONTEXT,
                                    entry.extension.getContext()
                            )
                    );
        }
        setChanged();
        notifyObservers();
        return entry.extension;
    }

    private ExtensionProxy loadExtension(Properties props) throws Exception {
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
                log.info("Instance name: '{}', Extension name: '{}', Version: '{}', Notes: '{}', License: '{}', Home: '{}', Author '{}', Build interface Version: '{}',  File: '{}', Initialized: '{}'",
                    emptyIfNull(context.get(Base.ContextKeys.INSTANCE_NAME)),
                    emptyIfNull(context.get(Base.ContextKeys.EXTENSION_NAME)),
                    emptyIfNull(context.get(Base.ContextKeys.VERSION)),
                    emptyIfNull(context.get(Base.ContextKeys.EXTENSION_NOTES)),
                    emptyIfNull(context.get(Base.ContextKeys.LICENSE)),
                    emptyIfNull(context.get(Base.ContextKeys.HOME_URL)),
                    emptyIfNull(context.get(Base.ContextKeys.AUTHOR)),
                    emptyIfNull(context.get(Base.ContextKeys.BUILD_INTERFACE_VERSION)),
                    entry.getFileName(),
                    entry.initialized
                        );
            }
        }
        log.info("End of enabled extensions list");
    }

    private Object emptyIfNull(Object value) {
        return value == null ? "" : value;
    }
}
