package org.ovirt.engine.core.authentication;

import static java.util.Arrays.sort;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides some methods useful for all the managers that need to use a configuration file.
 *
 * @param <O> the type of the managed object
 */
public abstract class Manager<O> {
    private static final Logger log = LoggerFactory.getLogger(Manager.class);

    // Names of the configuratoin parameters:
    private static final String ENABLED_PARAMETER = "enabled";
    private static final String MODULE_PARAMETER = "module";
    private static final String TYPE_PARAMETER = "type";
    private static final String NAME_PARAMETER = "name";

    /**
     * The concrete class of the interface of the factory class.
     */
    private Class<? extends Factory<O>> factoryInterface;

    /**
     * Here we store factories indexed by type.
     */
    private Map<String, Factory<O>> factoriesByType;

    /**
     * Here we store the objects, indexed by name.
     */
    private Map<String, O> objectsByName;

    /**
     * We need to remember which class loaders have already been used in order to avoid instantiating factories multiple
     * times.
     */
    private Set<ClassLoader> visitedClassLoaders;

    protected Manager(Class<? extends Factory<O>> factoryInterface) {
        // Save the factory interface class:
        this.factoryInterface = factoryInterface;

        // Create the indexes for factories and objects (note that these don't need to be concurrent hash maps because
        // we use the copy on write technique to avoid synchronization issues):
        factoriesByType = new HashMap<String, Factory<O>>();
        objectsByName = new HashMap<String, O>();

        // Create the set of already use class visitedClassLoaders:
        visitedClassLoaders = new HashSet<ClassLoader>();
    }

    /**
     * Register a factory. This factory will be used to create objects that match either the type or the class of the
     * factory.
     *
     * @param factory the factory to register
     */
    public synchronized void registerFactory(Factory<O> factory) {
        HashMap<String, Factory<O>> newFactories = new HashMap<String, Factory<O>>(factoriesByType);
        newFactories.put(factory.getType(), factory);
        factoriesByType = newFactories;
    }

    /**
     * Finds a factory using the given configuration. The factory will be located using the {@code type}, and
     * {@code module} properties. If the configuration file contains the {@code module} parameter the manager will try
     * to load that module. Then the factories including in the SPI configuration of the module will be loaded and
     * registered with their types. For example, if the configuration file contains the following:
     *
     * <pre>
     * type=example
     * module=com.example
     * </pre>
     *
     * The manager will first try to load the {@code com.example} module. All the factories inside it will be loaded and
     * registered. After that the manager will try to find a factory registered with the name {@code example}.
     *
     * @param config the configuration already loaded from the properties file
     * @return a reference to the factory or {@code null} if the factory can't be found for any reason
     */
    protected Factory<O> findFactory(Configuration config) {
        // This will be the result:
        Factory<O> factory = null;

        // If a module has been specified we will use the class loader of that module to load the factory, otherwise we
        // will use the context class loader or the loader of this class:
        synchronized (visitedClassLoaders) {
            ClassLoader classLoader = null;
            String spec = config.getInheritedString(MODULE_PARAMETER);
            if (spec != null) {
                Module module = loadModule(spec);
                if (module != null) {
                    classLoader = module.getClassLoader();
                }
            }
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }
            if (!visitedClassLoaders.contains(classLoader)) {
                loadFactories(classLoader);
            }
        }

        // Get the type that identifies the factory:
        String type = config.getInheritedString(TYPE_PARAMETER);
        if (type != null) {
            factory = factoriesByType.get(type);
            if (factory != null) {
                return factory;
            }
            log.warn(
                "Can't find factory for type \"{}\" as specified in configuration file \"{}\".",
                type, config.getFile().getAbsolutePath()
            );
        }

        return factory;
    }

    /**
     * Load a module.
     *
     * @param spec the specification (name and version) of the module
     * @return the reference to the loaded module or {@code null} if the module can't be loaded
     */
    private Module loadModule(String spec) {
        // Locate the module loader used by the manager, it may not exist if we are running without JBoss modules:
        ModuleLoader loader = ModuleLoader.forClass(this.getClass());
        if (loader == null) {
            log.error(
                "Module \"{}\" can't be loaded because the modules system isn't enabled.",
                spec
            );
            return null;
        }

        // Locate the module:
        ModuleIdentifier id = ModuleIdentifier.fromString(spec);
        try {
            return loader.loadModule(id);
        }
        catch (ModuleLoadException exception) {
            log.error(
                "Module \"{}\" can't be loaded.",
                spec, exception
            );
            return null;
        }
    }

    /**
     * Load all the factories contained in the SPI files of the class loader.
     *
     * @param classLoader the class loader
     */
    private void loadFactories(ClassLoader classLoader) {
        ServiceLoader<? extends Factory<O>> loader = ServiceLoader.load(factoryInterface, classLoader);
        for (Factory<O> factory : loader) {
            registerFactory(factory);
        }
    }

    /**
     * Load the configuration files and for each of them try to locate a factory that supports it and then use it to
     * create the object.
     *
     * @param directory the directory where the configuration files are stored
     * @throws ConfigurationException if something fails while loading the configuration files
     */
    public void loadFiles(File directory) throws ConfigurationException {
        // Check that the folder that contains the configuration files exists:
        if (!directory.exists()) {
            throw new ConfigurationException(
                "The directory \"" + directory.getAbsolutePath() + "\" containing the configuration files doesn't " +
                "exist."
            );
        }

        // The order of the files inside the directory is relevant, as the objects are created in the same order that
        // the files are processed, so it is better to sort them so that objects will always be created in the same
        // order regardless of how the filesystem decides to store the entries of the directory:
        File[] files = directory.listFiles();
        sort(files);
        for (File file : files) {
            if (file.getName().endsWith(".conf")) {
                loadFile(file);
            }
        }
    }

    /**
     * Load a configuration file, locate a factory that supports it and use it to create the object.
     *
     * @param file the file to load
     * @throws ConfigurationException if something fails while loading the configuration file
     */
    private void loadFile(File file) throws ConfigurationException {
        // Load the configuration file:
        Configuration config = null;
        try {
            config = Configuration.loadFile(file);
        }
        catch (IOException exception) {
            throw new ConfigurationException(
                "Can't load object configuration file \"" + file.getAbsolutePath()+ "\".",
                exception
            );
        }

        // Check if the object has been explicitly disabled, if it is then return immediately:
        Boolean enabled = config.getBoolean(ENABLED_PARAMETER);
        if (enabled != null && !enabled.booleanValue()) {
            return;
        }

        // Parse the configuration file and create the object:
        parseObject(config);
    }

    /**
     * Creates an object with the given configuration. It is protected because subclasses may want to expose it with
     * a more appropriate name, for example, a directory manager may expose it as follows:
     *
     * <pre>
     * public Directory parseDirectory(Configuration config) {
     *     return parseObject(config);
     * }
     * </pre>
     *
     * If the object is successfully created it will also be registered for future use.
     *
     * @param config the properties already loaded from the configuration file
     * @return the reference to the loaded object or {@code null} if no such object can be found or if there is any
     *     problem when trying to load it
     */
    protected O parseObject(Configuration config) throws ConfigurationException {
        // Verify that the configuration file contains an name for the object, otherwise it can't be created and
        // registered:
        String name = config.getInheritedString(NAME_PARAMETER);
        if (name == null) {
            throw new ConfigurationException(
                "Can't create object from configuration file \"" + config.getFile().getAbsolutePath() + "\" becuase " +
                "it doesn't contain the mandatory parameter \"" + config.getAbsoluteKey(NAME_PARAMETER) + "\"."
            );
        }

        // Find the factory that supports the type:
        Factory<O> factory = findFactory(config);
        if (factory == null) {
            throw new ConfigurationException(
                "Can't find any factory for the object configured " +
                "in file \"" + config.getFile().getAbsolutePath() + "\"."
            );
        }

        // Let the factory use the configuration to create the object:
        O object = factory.create(config);
        if (object == null) {
            log.error(
                "The factory failed to create the object configured in file \"{}\".",
                config.getFile().getAbsolutePath()
            );
            return null;
        }

        // Update the map of loaded objects:
        registerObject(name, object);

        return object;
    }

    /**
     * Returns an unmodifiable list containing all the managed objects that have been previously created. It is
     * protected to force subclasses to implement their our version of the method, with a more appropriate name, for
     * example, a manager of directory objects should include a method like this:
     *
     * <pre>
     * public List<Directory> getDirectories() {
     *     return getObjects();
     * }
     * </pre>
     */
    protected List<O> getObjects() {
        List<O> list = new ArrayList<O>(objectsByName.size());
        list.addAll(objectsByName.values());
        return Collections.unmodifiableList(list);
    }

    /**
     * Gets the object for the given name. It is protected to force subclasses to implement their own version of the
     * method, with a more appropriate name, for example, a manager of directory objects should include a method like
     * this:
     *
     * <pre>
     * public Directory getDirectory(String name) {
     *     return getObject(name);
     * }
     * </pre>
     *
     * @param name the name of the object
     * @return the requested object or {@code null} if no such object can be found
     */
    protected O getObject(String name) {
        // Check if there is a previously loaded object for the given name:
        O object = objectsByName.get(name);
        if (object != null) {
            return object;
        }

        // No luck, no object matches the given name:
        log.warn(
            "Can't find an object named \"" + name + "\"."
        );
        return null;
    }

    /**
     * Register an object with a name. Note that ff a name with the given object already exists it will be replaced.
     *
     * @param name th ename of the object to register
     * @param object the object to register
     */
    protected synchronized void registerObject(String name, O object) {
        HashMap<String, O> newObjects = new HashMap<String, O>(objectsByName);
        newObjects.put(name, object);
        objectsByName = newObjects;
    }

    /**
     * Forget all the factories and objects.
     */
    public synchronized void clear() {
        factoriesByType.clear();
        objectsByName.clear();
    }
}
