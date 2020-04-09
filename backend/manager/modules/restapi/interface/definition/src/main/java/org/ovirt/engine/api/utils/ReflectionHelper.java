/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

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
        } catch (Exception ignore) {
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
        return method != null ? method : getMethod(o, IS_ROOT + capitalizedName);
    }

    public static Method getSetter(Object o, String name) {
        String capitalizedName = capitalize(name);
        Method method = getMethod(o, SET_ROOT + capitalizedName);
        return method;
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
            if (m.getName().equalsIgnoreCase(name)) {
                ret = m;
                break;
            }
        }
        return ret;
    }
}

