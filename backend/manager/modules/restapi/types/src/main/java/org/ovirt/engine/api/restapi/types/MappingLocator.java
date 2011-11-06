package org.ovirt.engine.api.restapi.types;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Discovers and manages type mappers.
 */
public class MappingLocator {

    private String discoverPackageName;
    private Map<ClassPairKey, Mapper<?, ?>> mappers;

    /**
     * Normal constructor used when injected
     */
    public MappingLocator() {
        mappers = new HashMap<ClassPairKey, Mapper<?, ?>>();
    }

    /**
     * Constructor intended only for testing.
     *
     * @param discoverPackageName
     *            package to look under
     */
    MappingLocator(String discoverPackageName) {
        this.discoverPackageName = discoverPackageName;
        mappers = new HashMap<ClassPairKey, Mapper<?, ?>>();
    }

    /**
     * Discover mappers and populate internal registry. The classloading
     * environment is scanned for classes contained under the
     * org.ovirt.engine.api.restapi.types package and exposing methods decorated
     * with the @Mapping annotation.
     */
    public void populate() {
        List<Class<?>> classes = discoverClasses(discoverPackageName != null ? discoverPackageName
                : this.getClass().getPackage().getName());
        for (Class<?> clz : classes) {
            for (Method method : clz.getMethods()) {
                Mapping mapping = method.getAnnotation(Mapping.class);
                if (mapping != null) {
                    mappers.put(new ClassPairKey(mapping.from(), mapping.to()),
                            new MethodInvokerMapper(method, mapping.to()));
                }
            }
        }
    }

    /**
     * Get an appropriate mapper mediating between the required types.
     *
     * @param <F>
     *            the from type
     * @param <T>
     *            the to type
     * @param from
     *            the from class
     * @param to
     *            the to class
     * @return a mapped instance of the to type
     */
    @SuppressWarnings("unchecked")
    public <F, T> Mapper<F, T> getMapper(Class<F> from, Class<T> to) {
        return (Mapper<F, T>) mappers.get(new ClassPairKey(from, to));
    }

    /**
     * Discover classes under target package.
     *
     * @param packageName
     *            package to look under
     * @return list of classes found
     */
    private List<Class<?>> discoverClasses(String packageName) {
        List<Class<?>> ret = new ArrayList<Class<?>>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(toPath(packageName));
            List<File> dirs = new ArrayList<File>();
            List<JarInputStream> jars = new ArrayList<JarInputStream>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (isJar(resource)) {
                    jars.add(new JarInputStream(new FileInputStream(getJarName(resource))));
                } else if (containsJar(resource)) {
                    jars.add(getContainingResource(classLoader, resource));
                } else {
                    dirs.add(new File(resource.getFile()));
                }
            }
            walkJars(ret, packageName, jars);
            walkDirs(ret, packageName, dirs);
        } catch (Exception e) {
            ret = Collections.emptyList();
        }
        return ret;
    }

    private static JarInputStream getContainingResource(ClassLoader classLoader, URL resource)
            throws Exception {
        JarInputStream ret = null;
        Enumeration<URL> globals = classLoader.getResources("/");
        while (globals.hasMoreElements()) {
            URL global = globals.nextElement();
            if (resource.toString().startsWith(global.toString())) {
                ret = (JarInputStream)global.openStream();
                break;
            }
        }
        return ret;
    }

    private void walkJars(List<Class<?>> classList, String packageName, List<JarInputStream> jars)
            throws Exception {
        for (JarInputStream jarFile : jars) {
            try {
                JarEntry entry = null;
                while ((entry = jarFile.getNextJarEntry()) != null) {
                    String name = toPackage(entry.getName());
                    if (name.startsWith(packageName) && isClass(name)) {
                        classList.add(Class.forName(trimClass(name)));
                    }
                }
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }
        }
    }

    private void walkDirs(List<Class<?>> classList, String packageName, List<File> dirs)
            throws Exception {
        for (File directory : dirs) {
            List<Class<?>> classes = new ArrayList<Class<?>>();
            if (directory.exists()) {
                File[] files = directory.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        classes.addAll(getClassesUnder(file, in(packageName, file.getName())));
                    } else if (isClass(file.getName())) {
                        classes.add(Class.forName(in(packageName, trimClass(file.getName()))));
                    }
                }
                classList.addAll(getClassesUnder(directory, packageName));
            }
        }
    }

    private List<Class<?>> getClassesUnder(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(getClassesUnder(file, in(packageName, file.getName())));
            } else if (isClass(file.getName())) {
                classes.add(Class.forName(in(packageName, trimClass(file.getName()))));
            }
        }
        return classes;
    }

    private static String toPath(String packageName) {
        return packageName.replace('.', '/');
    }

    private static String toPackage(String path) {
        return path.replaceAll("/", "\\.");
    }

    private static String in(String packageName, String entry) {
        return packageName + '.' + entry;
    }

    private static String getJarName(URL resource) {
        return resource.getPath().split("!")[0].substring("file:".length());
    }

    private static boolean isJar(URL resource) {
        return resource.getProtocol().equals("jar");
    }

    private static boolean containsJar(URL resource) {
        return resource.getPath().indexOf(".jar") != -1;
    }

    private static boolean isClass(String s) {
        return s.endsWith(".class");
    }

    private static String trimClass(String s) {
        return s.substring(0, s.length() - 6);
    }

    private static class ClassPairKey {
        private Class<?> from, to;

        private ClassPairKey(Class<?> from, Class<?> to) {
            this.from = from;
            this.to = to;
        }

        public int hashCode() {
            return to.hashCode() + from.hashCode();
        }

        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other instanceof ClassPairKey) {
                ClassPairKey key = (ClassPairKey) other;
                return to == key.to && from == key.from;
            }
            return false;
        }

        public String toString() {
            return "map from: " + from + " to: " + to;
        }
    }

    private static class MethodInvokerMapper implements Mapper<Object, Object> {
        private Method method;
        private Class<?> to;

        private MethodInvokerMapper(Method method, Class<?> to) {
            this.method = method;
            this.to = to;
        }

        @Override
        public Object map(Object from, Object template) {
            Object ret = null;
            try {
                // REVISIT support non-static mapping methods also
                ret = method.invoke(null, from, template);
            } catch (Exception e) {
                // REVISIT logging, fallback null-mapping
                e.printStackTrace();
            }
            return to.cast(ret);
        }

        public String toString() {
            return "map to: " + to + " via " + method;
        }
    }
}
