/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageExplorer {
    /**
     * The logger used by this class.
     */
    private static final Logger log = LoggerFactory.getLogger(PackageExplorer.class);

    /**
     * Discover classes under target package.
     *
     * @param packageName the fully qualified name of the package
     * @return the list of class names found
     */
    public static List<String> discoverClasses(String packageName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> classNames = new ArrayList<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(toPath(packageName));
            List<File> dirs = new ArrayList<>();
            List<URL> jarUrls = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (isJar(resource)) {
                    jarUrls.add(resource);
                } else if (containsJar(resource)) {
                    URL jarRootUrl = getContainingResourceURL(classLoader, resource);
                    if (jarRootUrl != null) {
                        jarUrls.add(jarRootUrl);
                    }
                } else {
                    dirs.add(new File(URLDecoder.decode(resource.getFile(), "UTF-8")));
                }
            }
            IOException jarIssues = walkJars(classNames, packageName, jarUrls);
            walkDirs(classNames, packageName, dirs);
            if (jarIssues != null) {
                log.error(
                    "One or more jars failed to scan for package \"{}\".",
                    packageName,
                    jarIssues
                );
            }
        } catch (IOException exception) {
            log.error(
                "Error while trying to find scan classpath for package \"{}\".",
                packageName,
                exception
            );
        }
        return classNames;
    }

    private static URL getContainingResourceURL(ClassLoader classLoader, URL resource)
            throws IOException {
        Enumeration<URL> globals = classLoader.getResources("/");
        while (globals.hasMoreElements()) {
            URL global = globals.nextElement();
            if (resource.toString().startsWith(global.toString())) {
                return global;
            }
        }
        return null;
    }

    private static JarInputStream openJarStream(URL jarUrl) throws IOException {
        if (isJar(jarUrl)) {
            return new JarInputStream(new FileInputStream(URLDecoder.decode(getJarName(jarUrl), "UTF-8")));
        }
        return (JarInputStream) jarUrl.openStream();
    }

    private static IOException walkJars(List<String> classNames, String packageName, List<URL> jarUrls) {
        IOException aggregate = null;
        for (URL jarUrl : jarUrls) {
            try (JarInputStream jarFile = openJarStream(jarUrl)) {
                JarEntry entry;
                while ((entry = jarFile.getNextJarEntry()) != null) {
                    String name = toPackage(entry.getName());
                    if (name.startsWith(packageName) && isClass(name)) {
                        classNames.add(trimClass(name));
                    }
                }
            } catch (IOException e) {
                if (aggregate == null) {
                    aggregate = new IOException("Failed to scan one or more jars");
                }
                aggregate.addSuppressed(e);
            }
        }
        return aggregate;
    }

    private static void walkDirs(List<String> classNames, String packageName, List<File> dirs)
            throws IOException {
        for (File directory : dirs) {
            if (directory.exists()) {
                classNames.addAll(getClassesUnder(directory, packageName));
            }
        }
    }

    private static List<String> getClassesUnder(File directory, String packageName) {
        List<String> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(getClassesUnder(file, in(packageName, file.getName())));
                } else if (isClass(file.getName())) {
                    classes.add(in(packageName, trimClass(file.getName())));
                }
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
