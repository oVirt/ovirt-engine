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

import java.net.URI;
import java.lang.reflect.Method;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

public class ActionsBuilder {

    private UriBuilder uriBuilder;
    private Class<?> service;

    public ActionsBuilder(UriBuilder uriBuilder, Class<?> service) {
        this.uriBuilder = uriBuilder;
        this.service = service;
    }

    public Actions build() {
        Actions actions = null;

        for (Method method : service.getMethods()) {
            Path path = method.getAnnotation(Path.class);
            Actionable actionable = method.getAnnotation(Actionable.class);

            if (actionable != null && path != null) {
                URI uri = uriBuilder.clone().path(path.value()).build();

                Link link = new Link();
                link.setRel(path.value());
                link.setHref(uri.toString());

                if (actions == null) {
                    actions = new Actions();
                }
                actions.getLinks().add(link);
            }
        }

        return actions;
    }
}
