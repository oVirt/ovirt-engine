/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.api.v3.V3Adapter;

public class V3OutAdapters {
    private static Map<Class, V3Adapter> adapters = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <FROM, TO> TO adaptOut(FROM object) {
        if (object == null) {
            return null;
        }
        if (object instanceof byte[]) {
            return (TO) object;
        }
        if (object instanceof List) {
            List<Object> input = (List<Object>) object;
            List<Object> output = new ArrayList<>(input.size());
            for (Object item : input) {
                output.add(adaptOut(item));
            }
            return (TO) output;
        }
        Class<?> objectClass = object.getClass();
        V3Adapter<FROM, TO> adapterInstance = adapters.get(objectClass);
        if (adapterInstance == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(V3InAdapters.class.getPackage().getName());
            buffer.append(".");
            buffer.append("V3");
            buffer.append(objectClass.getSimpleName());
            buffer.append("OutAdapter");
            String adapterClassName = buffer.toString();
            Class<?> adapterClass;
            try {
                adapterClass = Class.forName(adapterClassName);
            }
            catch (ClassNotFoundException exception) {
                throw new RuntimeException(
                    "Can't find V3 output adapter of class \"" + adapterClassName + "\" for object of " +
                    "class \"" + objectClass.getName() + "\"",
                    exception
                );
            }
            try {
                adapterInstance = (V3Adapter<FROM, TO>) adapterClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException exception) {
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
