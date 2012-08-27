package org.ovirt.engine.api.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PackageExplorer {

    /**
     * Discover classes under target package.
     *
     * @param packageName
     *            package to look under
     * @return list of classes found
     */
    public static List<Class<?>> discoverClasses(String packageName) {
        List<Class<?>> ret = new ArrayList<Class<?>>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(toPath(packageName));
            List<File> dirs = new ArrayList<File>();
            List<JarInputStream> jars = new ArrayList<JarInputStream>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (isJar(resource)) {
                    jars.add(new JarInputStream(new FileInputStream(URLDecoder.decode(getJarName(resource), "UTF-8"))));
                } else if (containsJar(resource)) {
                    jars.add(getContainingResource(classLoader, resource));
                } else {
                    dirs.add(new File(URLDecoder.decode(resource.getFile(), "UTF-8")));
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
                ret = (JarInputStream) global.openStream();
                break;
            }
        }
        return ret;
    }

    private static void walkJars(List<Class<?>> classList, String packageName, List<JarInputStream> jars)
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

    private static void walkDirs(List<Class<?>> classList, String packageName, List<File> dirs)
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

    private static List<Class<?>> getClassesUnder(File directory, String packageName)
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
}
