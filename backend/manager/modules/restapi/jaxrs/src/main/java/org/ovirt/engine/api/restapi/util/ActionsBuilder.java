/*
Copyright (c) 2010-2016 Red Hat, Inc.

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

package org.ovirt.engine.api.restapi.util;

import java.lang.reflect.Method;
import javax.ws.rs.Path;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Actions;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.utils.ArrayUtils;

public class ActionsBuilder {

    private static final String URL_SEPARATOR  = "/";
    private Class<?> service;
    private Class<?> collection;
    private String base;

    public ActionsBuilder(String base, Class<?> service) {
        this.base = base;
        this.service = service;
    }

    public ActionsBuilder(String base, Class<?> service, Class<?> collection) {
        this.base = base;
        this.service = service;
        this.collection = collection;
    }

    public Actions build() {
        Actions actions = null;

        for (Method method : ArrayUtils.concat(service.getMethods(), getInterfaceSignatures(collection))) {
            Path path = method.getAnnotation(Path.class);
            Actionable actionable = method.getAnnotation(Actionable.class);

            if (actionable != null && path != null) {
                Link link = new Link();
                link.setRel(path.value());
                link.setHref(base + URL_SEPARATOR + link.getRel());
                if (actions == null) {
                    actions = new Actions();
                }
                actions.getLinks().add(link);
            }
        }

        return actions;
    }

    private Method[] getInterfaceSignatures(Class<?> collection) {
        Method[] methods = new Method[0];
        if (collection != null){
            for (Class<?> inter : collection.getInterfaces()){
                methods = ArrayUtils.concat(methods, inter.getMethods());
            }
        }
        return methods;
    }
}
