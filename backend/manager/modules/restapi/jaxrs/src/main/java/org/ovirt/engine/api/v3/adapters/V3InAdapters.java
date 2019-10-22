/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.api.v3.V3Adapter;

public class V3InAdapters {
    private static Map<Class, V3Adapter> adapters = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <FROM, TO> TO adaptIn(FROM object) {
        if (object == null) {
            return null;
        }
        if (object instanceof List) {
            List<Object> input = (List<Object>) object;
            List<Object> output = new ArrayList<>(input.size());
            for (Object item : input) {
                output.add(adaptIn(item));
            }
            return (TO) output;
        }
        Class<?> objectClass = object.getClass();
        V3Adapter<FROM, TO> adapterInstance = adapters.get(objectClass);
        if (adapterInstance == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(V3InAdapters.class.getPackage().getName());
            buffer.append(".");
            buffer.append(objectClass.getSimpleName());
            buffer.append("InAdapter");
            String adapterClassName = buffer.toString();
            Class<?> adapterClass;
            try {
                adapterClass = Class.forName(adapterClassName);
            } catch (ClassNotFoundException exception) {
                throw new RuntimeException(
                    "Can't find V3 input adapter of class \"" + adapterClassName + "\" for object of " +
                    "class \"" + objectClass.getName() + "\"",
                    exception
                );
            }
            try {
                adapterInstance = (V3Adapter<FROM, TO>) adapterClass.newInstance();
            } catch (InstantiationException | IllegalAccessException exception) {
                throw new RuntimeException(
                    "Can't create V3 input adapter of class \"" + adapterClassName + "\" for object of " +
                    "class \"" + objectClass.getName() + "\"",
                    exception
                );
            }
            adapters.put(objectClass, adapterInstance);
        }
        return adapterInstance.adapt(object);
    }
}
