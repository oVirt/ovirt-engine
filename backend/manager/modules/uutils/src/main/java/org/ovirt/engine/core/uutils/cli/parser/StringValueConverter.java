package org.ovirt.engine.core.uutils.cli.parser;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StringValueConverter {

    private static final Map<Class<?>, Class<?>> typeBox = new HashMap<>();
    static {
        typeBox.put(boolean.class, Boolean.class);
        typeBox.put(byte.class, Byte.class);
        typeBox.put(char.class, Character.class);
        typeBox.put(double.class, Double.class);
        typeBox.put(float.class, Float.class);
        typeBox.put(int.class, Integer.class);
        typeBox.put(long.class, Long.class);
        typeBox.put(short.class, Short.class);
        typeBox.put(void.class, Void.class);
    }

    public static Object getObjectValueByString(Class<?> clazz, String value) {
        Object v = null;

        if (clazz.isPrimitive()) {
            clazz = typeBox.get(clazz);
        }

        if (clazz.equals(Collection.class)) {
            List<Object> r = new ArrayList<>();
            for (String c : value.trim().split(" *, *")) {
                if (!c.isEmpty()) {
                    r.add(getObjectValueByString(String.class, c));
                }
            }
            v = r;
        }

        if (v == null) {
            if (clazz.isArray() && Object.class.isAssignableFrom(clazz.getComponentType())) {
                List<Object> r = new ArrayList<>();
                for (String c : value.trim().split(" *, *")) {
                    if (!c.isEmpty()) {
                        r.add(getObjectValueByString(clazz.getComponentType(), c));
                    }
                }
                v = (Object)r.toArray((Object[]) Array.newInstance(clazz.getComponentType(), 0));
            }
        }

        if (v == null) {
            try {
                Field f = clazz.getField(value);
                if (Modifier.isStatic(f.getModifiers())) {
                    v = f.get(null);
                }
            } catch(ReflectiveOperationException e) {}
        }

        if (v == null) {
            try {
                Method convert = clazz.getMethod("valueOf", String.class);
                if (Modifier.isStatic(convert.getModifiers())) {
                    v = convert.invoke(null, value);
                }
            } catch(ReflectiveOperationException e) {}
        }

        if (v == null) {
            try {
                Method convert = clazz.getMethod("valueOf", Object.class);
                if (Modifier.isStatic(convert.getModifiers())) {
                    v = convert.invoke(null, value);
                }
            } catch(ReflectiveOperationException e) {}
        }

        if (v == null) {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
                v = constructor.newInstance(value);
            } catch(ReflectiveOperationException e) {}
        }

        return v;
    }

}
