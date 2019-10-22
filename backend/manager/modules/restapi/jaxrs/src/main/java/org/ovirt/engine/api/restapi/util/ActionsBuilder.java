/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
