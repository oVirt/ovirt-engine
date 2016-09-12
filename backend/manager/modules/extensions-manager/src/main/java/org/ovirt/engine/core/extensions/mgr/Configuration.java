package org.ovirt.engine.core.extensions.mgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class Configuration {
    /**
     * Load the configuration from a properties file.
     *
     * @param file the properties file
     * @throws IOException if anything fails while loading the properties file
     */
    public static Configuration loadFile(File file) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            properties.load(in);
        }
        return new Root(file, properties);
    }

    /**
     * This is a cache of prefix views used to avoid creating the same view object multiple times.
     */
    private Map<String, Configuration> prefixViews;

    /**
     * This is a cache of typed views used to avoid creating the same view object multiple times.
     */
    private Map<Class<?>, Object> typedViews;

    /**
     * Create a new empty configuration object. Subclasses should provide their own constructor, and the first thing it
     * should do is call this one.
     */
    protected Configuration() {
        prefixViews = new HashMap<>();
        typedViews = new HashMap<>();
    }

    /**
     * Gets the file that this configuration has been loaded from.
     *
     * @return the reference to the file object
    */
    public abstract File getFile();

    /**
     * Gets the properties object that is associated with the configuration hierarchy
     *
     * @return the reference to the properties object
     */
    public abstract Properties getProperties();

    /**
     * Gets the value associated to a configuration parameter, searching it only the this configuration.
     *
     * @param key the name of the configuration parameter
     * @return the value of the configuration parameter or {@code null} if no such parameter exists
     */
    public abstract String getString(String key);

    /**
     * Get the list of the parameter names contained in this configuration object.
     *
     * @return a list containing the key names, with no particular order
     */
    public abstract List<String> getKeys();

    /**
     * This method calculates complete key names. It is intended basically for log messages. For example, if the
     * configuration used to build an object is a view of another configuration with the prefix {@code directory} and
     * we are using a key named {@code name} we want to display in the logs {@code directory.name} and not just
     * {@code name}.
     *
     * @param key the relative key name
     * @return the absolute key name, including all the prefixes if the configuration is a subset of another
     *     configuration
     */
    public abstract String getAbsoluteKey(String key);

    /**
     * Returns a configuration object that represents a view of the parameters that start with a given prefix. These
     * objects are cached, so if the same prefix is requested twice the same object will be returned.
     *
     * @param prefix the prefix
     * @return the configuration object, will never be {@code null}
     */
    public synchronized Configuration getView(String prefix) {
        Configuration view = prefixViews.get(prefix);
        if (view == null) {
            view = new View(this, prefix);
            prefixViews.put(prefix, view);
        }
        return view;
    }

    /**
     * Returns an object that implements the given interface and whose getters obtain the values from the given
     * configuration. For example, if you have the need to manage two parameters containing a host name and
     * a port number you can you can create a {@code Address} interface like this:
     *
     * <pre>
     * public interface Address {
     *     public String getHost();
     *     public int getPort();
     * }
     * </pre>
     *
     * When using this interface the names of the parameters will be derived from the names of the Java Beans properties
     * of the interface, in this case they will be {@code host} and {@code port}. To actually use this interface write
     * something like this:
     *
     * <pre>
     * Configuration config = ...;
     * Address address = config.getView("server", Address.class);
     * System.out.println(address.getHost());
     * System.out.println(address.getPort());
     * </pre>
     *
     * It is equivalent to this:
     *
     * <pre>
     * Configuration config = ...;
     * Configuration address = config.getView("server");
     * System.out.println(address.getString("host"));
     * System.out.println(address.getInt("port"));
     * </pre>
     *
     * @param type the class object corresponding to the interface type that defines view
     * @param <V> the interface type that defines the view
     * @return the configuration object, will never be {@code null}
     */
    public synchronized <V> V getView(Class<V> type) {
        Object view = typedViews.get(type);
        if (view == null) {
            view = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, new Handler(this));
            typedViews.put(type, view);
        }
        return (V) view;
    }

    /**
     * Gets the parent configuration.
     *
     * @return the parent configuration if this is a view or {@code null} if this is a root configuration
     */
    public Configuration getParent() {
        return null;
    }

    /**
     * Checks if this configuration object contains any parameter.
     *
     * @return {@code true} if the configuration object doesn't contain any parameter, {@code false} otherwise
     */
    public boolean isEmpty() {
        return getKeys().isEmpty();
    }

    public boolean getBoolean(String key, boolean defaultValueIfNull) {
        String image = getString(key);
        if (image == null) {
            return defaultValueIfNull;
        }
        return Boolean.parseBoolean(image);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public Integer getInteger(String key) {
        String image = getString(key);
        if (image == null) {
            return null;
        }
        return Integer.parseInt(image);
    }

    public String[] getArray(String key) {
        String image = getString(key);
        if (image == null) {
            return null;
        }
        return image.split(",");
    }

    public List<String> getList(String key) {
        String[] array = getArray(key);
        if (array == null) {
            return null;
        }
        return Arrays.asList(array);
    }

    public <E extends Enum<E>> E getEnum(Class<E> type, String key) {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        return Enum.valueOf(type, value);
    }

    public File getFile(String key) {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        return new File(value);
    }

    /**
     * This class is the root of the hierarchy, backed by a simple properties object.
     */
    private static class Root extends Configuration {
        /**
         * The file that the configuration has been loaded from.
         */
        private File file;

        /**
         * The actual values of the configuration are stored in this properties object.
         */
        private Properties properties;

        /**
         * Creates a configuration object that serves as the root of a hierarchy.
         */
        private Root(File sourceFile, Properties properties) {
            super();
            this.properties = properties;
            this.file = sourceFile;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public File getFile() {
            return file;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Configuration getParent() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getString(String key) {
            return properties.getProperty(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getAbsoluteKey(String key) {
            return key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<String> getKeys() {
            List<String> keys = new LinkedList<>();
            Enumeration<?> elements = properties.propertyNames();
            while (elements.hasMoreElements()) {
                String next = (String) elements.nextElement();
                keys.add(next);
            }
            return Collections.unmodifiableList(keys);
        }

        @Override
        public Properties getProperties() {
            return properties;
        }
    }

    /**
     * This class is a view on top of another configuration. It view consist on adding a prefix to all the parameters.
     */
    private static class View extends Configuration {
        // This configuration doesn't actually store anything, it all comes from a parent configuration adding a prefix
        // to all the parameters:
        private Configuration parent;
        private String prefix;

        /**
         * Creates a configuration object that represents the subset of parameters that start with the given prefix.
         * When a parameter is requested from this configuration object the prefix will be added to the given name and
         * the value will then be obtained from the parent.
         *
         * @param parent the parent configuration object
         * @param prefix the prefix that will be added to parameter names before retrieving values from the parent
         *     configuration object
         */
        private View(Configuration parent, String prefix) {
            this.parent = parent;
            this.prefix = prefix;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public File getFile() {
            return parent.getFile();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Configuration getParent() {
            return parent;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getString(String key) {
            return parent.getString(prefix + "." + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getAbsoluteKey(String key) {
            return parent.getAbsoluteKey(prefix + "." + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<String> getKeys() {
            List<String> keys = new LinkedList<>();
            for (String key : parent.getKeys()) {
                if (key.startsWith(prefix + ".")) {
                    key = key.substring(prefix.length() + 1);
                    keys.add(key);
                }
            }
            return Collections.unmodifiableList(keys);
        }

        @Override
        public Properties getProperties() {
            return parent.getProperties();
        }
    }

    /**
     * This class handles invocations of typed configurations.
     */
    private static class Handler implements InvocationHandler {
        private static final String GET = "get";
        private static final String IS = "is";

        /**
         * This is the raw configuration handled by this instance.
         */
        private Configuration config;

        public Handler(Configuration config) {
            this.config = config;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Get key from the name of the method:
            String key = getKey(method);

            // Convert the value to the return type of the method:
            Class<?> returnType = method.getReturnType();
            if (returnType == String.class) {
                return config.getString(key);
            }
            if (returnType == Boolean.class || returnType == Boolean.TYPE) {
                return config.getBoolean(key);
            }
            if (returnType == Integer.class || returnType == Integer.TYPE) {
                return config.getInteger(key);
            }
            if (returnType.isEnum()) {
                return config.getEnum((Class<Enum>) returnType, key);
            }
            if (returnType.isArray() && returnType.getComponentType() == String.class) {
                return config.getArray(key);
            }
            if (returnType == List.class) {
                return config.getList(key);
            }
            if (returnType == File.class) {
                return config.getFile(key);
            }
            if (returnType.isInterface()) {
                return config.getView(key).getView(returnType);
            }

            // If we haven't converted the value before it means that it isn't supported:
            throw new IllegalArgumentException(
                "The return type of method \"" + method.getName() + "\" of view " +
                 "class \"" + method.getDeclaringClass().getName() + "\" isn't supported."
            );
        }

        /**
         * Calculates the name of the configuration key from the name of the getter.
         *
         * @param method the reference to the getter
         * @return the name of the key
         */
        private String getKey(Method method) {
            String name = method.getName();
            if (name.startsWith(GET)) {
                return name.substring(GET.length()).toLowerCase();
            }
            if (name.startsWith(IS)) {
                return name.substring(IS.length()).toLowerCase();
            }
            throw new IllegalArgumentException(
                "The method \"" + name + "\" of view class \"" + method.getDeclaringClass().getName() + "\" isn't a " +
                "a valid getter, it doesn't start with \"" + GET + "\" or \"" + IS + "\"."
            );
        }
    }
}

