package org.ovirt.engine.core.uutils.cli.parser;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ClassUtils;

class StringValueConverter {
    public static Object getObjectValueByString(Class<?> clazz, String value) {
        Object v = null;

        if (clazz.isPrimitive()) {
            clazz = ClassUtils.primitiveToWrapper(clazz);
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
                v = r.toArray((Object[]) Array.newInstance(clazz.getComponentType(), 0));
            }
        }

        if (v == null) {
            try {
                Field f = clazz.getField(value);
                if (Modifier.isStatic(f.getModifiers())) {
                    v = f.get(null);
                }
            } catch(ReflectiveOperationException ignore) {}
        }

        if (v == null) {
            try {
                Method convert = clazz.getMethod("valueOf", String.class);
                if (Modifier.isStatic(convert.getModifiers())) {
                    v = convert.invoke(null, value);
                }
            } catch(ReflectiveOperationException ignore) {}
        }

        if (v == null) {
            try {
                Method convert = clazz.getMethod("valueOf", Object.class);
                if (Modifier.isStatic(convert.getModifiers())) {
                    v = convert.invoke(null, value);
                }
            } catch(ReflectiveOperationException ignore) {}
        }

        if (v == null) {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
                v = constructor.newInstance(value);
            } catch(ReflectiveOperationException ignore) {}
        }

        if (v == null) {
            throw new IllegalArgumentException(String.format("Failed to convert '%s' to %s", value, clazz.getName()));
        }

        return v;
    }
}
