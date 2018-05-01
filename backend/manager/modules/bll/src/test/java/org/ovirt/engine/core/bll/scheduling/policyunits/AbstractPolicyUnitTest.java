package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class AbstractPolicyUnitTest {
    String fieldToSetterName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.ENGLISH);

    @SuppressWarnings("unchecked")
    Object convert(Class<?> type, String value, Map<Guid, BusinessEntity<Guid>> cache) throws ParseException {
        /* Direct assignment possible */
        if (type.equals(String.class)) {
            return value;
        }

        /* Primitive values */
        if (type.equals(int.class)) {
            return Integer.valueOf(value);
        } else if (type.equals(float.class)) {
            return Float.valueOf(value);
        } else if (type.equals(double.class)) {
            return Double.valueOf(value);
        } else if (type.equals(boolean.class)) {
            return Boolean.valueOf(value);
        } else if (type.equals(long.class)) {
            return Long.valueOf(value);
        } else if (type.equals(Date.class)) {
            return TIME_FORMAT.parse(value);
        }

        /* Relationships between entities */
        if (BusinessEntity.class.isAssignableFrom(type)
                && cache.containsKey(new Guid(value))) {
            return cache.get(new Guid(value));
        }

        /* This supports string, integers, floats */
        try {
            Method converter = type.getMethod("valueOf", String.class);
            if (converter != null) {
                return converter.invoke(type, value);
            }
        } catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException ex) {
            // ignore and try the next method
        }

        /* Fallback tries to use a constructor(String) to prepare the value,
         * this is needed for Guid
         * */
        try {
            Constructor<?> constructor = type.getConstructor(String.class);
            if (constructor == null) {
                return null;
            }
            return constructor.newInstance(value);
        } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException ex) {
            // ignore and try the next method
        }

        return null;
    }

    <E> E setField(E entity, String fieldName, String value, Map<Guid, BusinessEntity<Guid>> cache)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException,
            ParseException {
        String[] components = fieldName.split("\\.");

        /* The last intermediate object */
        Object ptr = entity;

        /* Make sure all intermediate objects exist */
        for (int i=0; i<components.length - 1; i++) {
            Field f = ptr.getClass().getField(components[i]);
            if (f.get(ptr) == null) {
                Class<?> fieldType = f.getType();
                Object tmp = fieldType.newInstance();
                f.set(ptr, tmp);
                ptr = tmp;
            } else {
                ptr = f.get(ptr);
            }
        }

        /* Set the value */
        Class<?> type;

        /* Look up a setter method */
        String setterName = fieldToSetterName(components[components.length - 1]);
        for (Method m: ptr.getClass().getMethods()) {
            if (!m.getName().equals(setterName)) {
                continue;
            }

            if (m.getParameterTypes().length != 1) {
                continue;
            }

            /* Try converting the value to the requested type */
            Object candidateValue = convert(m.getParameterTypes()[0], value, cache);
            if (candidateValue == null) {
                continue;
            }

            m.invoke(ptr, candidateValue);
            return entity;
        }

        /* Use field introspection as a backup */
        Field f = ptr.getClass().getField(components[components.length - 1]);
        type = f.getType();
        Object realValue = convert(type, value, cache);
        if (realValue == null) {
            throw new NoSuchFieldException();
        }
        f.set(ptr, realValue);

        return entity;
    }

    <T extends BusinessEntity<Guid>> Map<Guid, T> loadEntities(Class<T> type, String fixtureName, Map<Guid, BusinessEntity<Guid>> cache)
            throws NoSuchFieldException, InvocationTargetException, InstantiationException,
            IllegalAccessException, IOException, ParseException {
        Map<Guid, T> entities = new HashMap<>();
        InputStream inputStream = getClass().getResourceAsStream("/scheduling/" + fixtureName);

        // Make sure the reader is closed
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            /* First line contains the field names */
            String header = bufferedReader.readLine();
            String[] fields = header.split(",");

            String line = bufferedReader.readLine();
            while (line != null) {
                T entity = type.newInstance();
                String[] values = line.split(",");
                for (int i = 0; i < Math.min(values.length, fields.length); i++) {
                    String value = values[i].trim();
                    if (value.isEmpty()) {
                        continue;
                    }
                    setField(entity, fields[i], value, cache);
                }
                entities.put(entity.getId(), entity);
                cache.put(entity.getId(), entity);
                line = bufferedReader.readLine();
            }

            return entities;
        }
    }

    public Map<Guid, VDS> loadHosts(String fixtureName, Map<Guid, BusinessEntity<Guid>> cache)
            throws InstantiationException, IOException, IllegalAccessException,
            InvocationTargetException, NoSuchFieldException, ParseException {
        return loadEntities(VDS.class, fixtureName, cache);
    }

    public Map<Guid, VM> loadVMs(String fixtureName, Map<Guid, BusinessEntity<Guid>> cache)
            throws InstantiationException, IOException, IllegalAccessException,
            InvocationTargetException, NoSuchFieldException, ParseException {
        return loadEntities(VM.class, fixtureName, cache);
    }

    public Map<Guid, BusinessEntity<Guid>> newCache() {
        return new HashMap<>();
    }
}
