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

package org.ovirt.engine.api.model;

import java.lang.reflect.Method;
import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.utils.ArrayUtils;


public class ActionsBuilder {

    private static final String URL_SEPARATOR  = "/";
    private UriBuilder uriBuilder;
    private Class<?> service;
    UriInfo uriInfo;
    Class<?> collection;

    public ActionsBuilder(UriBuilder uriBuilder, Class<?> service) {
        this.uriBuilder = uriBuilder;
        this.service = service;
    }

    public ActionsBuilder(UriInfo uriInfo, Class<?> service, Class<?> collection) {
        this.uriInfo = uriInfo;
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
                if (uriBuilder != null) {
                    URI uri = uriBuilder.clone().path(path.value()).build();
                    link.setHref(uri.toString());
                } else {
                    link.setHref(this.uriInfo.getBaseUri().getPath() +
                                 this.uriInfo.getPath().substring(1) +
                                 URL_SEPARATOR +
                                 link.getRel());
                }
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
