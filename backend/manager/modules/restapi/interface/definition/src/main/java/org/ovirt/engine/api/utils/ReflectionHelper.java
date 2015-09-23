/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.ovirt.engine.api.model.BaseResource;

public class ReflectionHelper {

    private static final String GET_ROOT = "get";
    private static final String SET_ROOT = "set";
    private static final String IS_ROOT = "is";
    private static final String IS_SET_ROOT = "isSet";

    private ReflectionHelper() {}

    public static <R extends BaseResource> R newModel(Class<R> clz) {
        R ret = null;
        try {
            ret = clz.newInstance();
        } catch (Exception e) {
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static <R extends BaseResource> R newModel(Object resource) {
        return newModel((Class<R>) ((ParameterizedType) resource.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public static <R extends BaseResource> R assignChildModel(BaseResource parent, Class<R> childType) {
        Method setter = getMethod(parent,
                                  SET_ROOT + capitalize(childType.getSimpleName().toLowerCase()));

        R child = newModel(childType);
        try {
            setter.invoke(parent, child);
        } catch (Exception e) {
            // InvocationTargetException etc. should never occur
            // as this is a simple setter
        }

        return child;
    }

    public static String capitalize(String s) {
        return Character.isLowerCase(s.charAt(0))
               ? s.substring(0, 1).toUpperCase() + s.substring(1)
               : s;
    }

    public static boolean isSet(Object o, String name) {
        boolean set = false;
        if(o != null){
            Method m = getMethod(o, IS_SET_ROOT + name);
            Object ret = invoke(o, m);
            if (ret != null && ret instanceof Boolean && ((Boolean) ret).booleanValue()) {
                // (isSetX() method only tells us if the value is not null).
                // for Strings we also have to check that the value is not empty.
                if (getReturnType(o, name).equals(String.class)) {
                    Object result = invoke(o, getGetter(o, name));
                    String resultAsString = (String) result;
                    if (!resultAsString.isEmpty()) {
                        set = true;
                    }
                } else {
                    set = true;
                }
            }
        }
        return set;
    }

    public static Method getGetter(Object o, String name) {
        String capitalizedName = capitalize(name);
        Method method = getMethod(o, GET_ROOT + capitalizedName);
        return (method != null ? method : getMethod(o, IS_ROOT + capitalizedName));
    }

    public static Class<?> getReturnType(Object o, String name) {
        Method getter = getGetter(o, name);
        return getter.getReturnType();
    }

    public static boolean different(Object lhs, Object rhs, String name) {
        Method lhsm = getMethod(lhs, GET_ROOT + name);
        Method rhsm = getMethod(rhs, GET_ROOT + name);
        Object lhsr = lhsm != null ? invoke(lhs, lhsm) : null;
        Object rhsr = rhsm != null ? invoke(rhs, rhsm) : null;
        return !(lhsr == null || lhsr.equals(rhsr));
    }

    public static Object invoke(Object o, Method m) {
        Object ret = null;
        try {
            ret = m.invoke(o);
        } catch (Exception e) {
            // InvocationTargetException etc. should never occur
            // as this is a simple getter
        }
        return ret;
    }

    public static boolean set(Object o, String field, String value) {
        boolean success = false;
        String name = SET_ROOT + capitalize(field);
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name) && isPrimitive(m)) {
                Object arg = isString(m)
                             ? value
                             : isBoolean(m)
                               ? Boolean.parseBoolean(value)
                               : isInteger(m)
                                 ? Integer.parseInt(value)
                                 : null;
                if (arg != null) {
                    try {
                        m.invoke(o, arg);
                        success = true;
                    } catch (Exception e) {
                        // InvocationTargetException etc. should never occur
                        // as this is a simple getter
                    }
                }
                break;
            }
        }
        return success;
    }

    public static Object get(Object o, String field) {
        Object ret = null;
        String name = GET_ROOT + capitalize(field);
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                try {
                    ret = m.invoke(o);
                } catch (Exception e) {
                    // InvocationTargetException etc. should never occur
                    // as this is a simple getter
                }
                break;
            }
        }
        return ret;
    }

    private static boolean isPrimitive(Method m) {
        Class<?>[] params = m.getParameterTypes();
        return params.length == 1
               && (String.class.equals(params[0])
                   || Boolean.TYPE.equals(params[0])
                   || Integer.TYPE.equals(params[0]));
    }

    private static boolean isString(Method m) {
        return String.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean isInteger(Method m) {
        return Integer.TYPE.equals(m.getParameterTypes()[0]);
    }

    private static boolean isBoolean(Method m) {
        return Boolean.TYPE.equals(m.getParameterTypes()[0]);
    }

    private static Method getMethod(Object o, String name) {
        Method ret = null;
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                ret = m;
                break;
            }
        }
        return ret;
    }
    /**
     * Locate all directories in given package
     *
     * @param path
     * @return Map<URL, File>
     * @throws IOException
     */
    public static List<URL> getDirectories(String path) throws IOException {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         assert classLoader != null;
         Enumeration<URL> resources = classLoader.getResources(path);
         List<URL> dirs = new ArrayList<URL>();
         while (resources.hasMoreElements()) {
             dirs.add(resources.nextElement());
         }
         return dirs;
    }
    /**
     * Locates all classes in given package
     *
     * @param  packageName
     * @return array of the classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException,
            IOException {
        String path = packageName.replace('.', '/');
        List<URL> dirs = getDirectories(path);
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        ClassLoader loader = URLClassLoader.newInstance(dirs.toArray(new URL[0]),
                                                        Thread.currentThread().getContextClassLoader());

        for (URL directory : dirs) {
            String resource = URLDecoder.decode(directory.getPath(), "UTF-8").replace("/"+path+"/", "");
            if (resource.endsWith(".jar")) {
                classes.addAll(getClassNamesInJarPackage(loader, resource, packageName));
            } else {
                classes.addAll(getClassNamesInPackage(new File(URLDecoder.decode(directory.getFile(), "UTF-8")), packageName));
            }
        }
        return classes;
    }
    /**
     * Locates all classes in given package
     *
     * @param  directory
     * @param  packageName
     * @return List of classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> getClassNamesInPackage(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    assert !file.getName().contains(".");
                    classes.addAll(getClassNamesInPackage(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6)));
                }
            }
        }
        return classes;
    }

    /**
     * Locates all classes in given jar package
     *
     * @param  jarName
     * @param  url
     * @param  packageName
     * @return List of classes
     * @throws MalformedURLException
     */
    static List<Class<?>> getClassNamesInJarPackage(ClassLoader loader, String jarName, String packageName) throws MalformedURLException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

        packageName = packageName.replaceAll("\\.", "/");

        JarInputStream jarFileInputStream = null;
        try {
            jarFileInputStream = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            while (true) {
                jarEntry = jarFileInputStream.getNextJarEntry();
                if (jarEntry == null) break;
                if ((jarEntry.getName().startsWith(packageName)) && (jarEntry.getName().endsWith(".class"))) {
                    classes.add(loader.loadClass(jarEntry.getName().replaceAll("/", "\\.").replace(".class", "")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(jarFileInputStream != null) {
                    jarFileInputStream.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
        return classes;
    }
}

