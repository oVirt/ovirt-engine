package org.ovirt.engine.ui.frontend.server.gwt.hibernate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {
    private static final Map<Class<?>, List<Method>> cachedMethods = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<Method, Method>> cachedSetterMethods = new ConcurrentHashMap<>();

    public static List<Method> getGetters(Class<?> clazz) {
        List<Method> getters = cachedMethods.get(clazz);
        if (getters == null) {
            getters = new ArrayList<>();
            for (Method method : clazz.getMethods()) {
                if (isGetter(method)) {
                    getters.add(method);
                }
            }
            cachedMethods.put(clazz, getters);
        }
        return getters;
    }

    private static boolean isGetter(Method method) {
        String name = method.getName();
        if (!name.startsWith("get")) {//$NON-NLS-1$
            return false;
        }
        if (name.length() == 3) {
            return false;
        }
        if (name.equals("getClass")) {//$NON-NLS-1$
            return false;
        }
        if (void.class.equals(method.getReturnType())) {
            return false;
        }
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        return true;
    }

    public static Object get(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setIfPossible(Object object, Method getter, Object value) {
        try {
            Class<? extends Object> clazz = object.getClass();
            Method setterMethod = getSetterMethod(clazz, getter, value.getClass());
            if (setterMethod == null) {
                return;
            }
            setterMethod.invoke(object, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getSetterMethod(Class<?> clazz, Method getter, Class<?> parameterClass) {
        Map<Method, Method> setterMap = cachedSetterMethods.get(clazz);
        if (setterMap == null) {
            setterMap = new ConcurrentHashMap<>();
            cachedSetterMethods.put(clazz, setterMap);
        }
        Method setter = setterMap.get(getter);
        if (setter == null) {
            String getterMethodName = getter.getName();
            String setterMethodName = "s" + getterMethodName.substring(1, getterMethodName.length()); //$NON-NLS-1$
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(setterMethodName)) {
                    setter = method;
                    setterMap.put(getter, setter);
                    break;
                }
            }
        }
        return setter;
    }
}
