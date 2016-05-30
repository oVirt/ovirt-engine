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

package org.ovirt.engine.api.v3;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.servers.V3SystemServer;

/**
 * This is the entry point for version 3 of the API.
 */
@ApplicationPath("/v3")
public class V3Application extends Application {
    private final Set<Object> singletons = new HashSet<>();

    public V3Application() throws Exception {
        // Add the root server:
        V3SystemServer server = new V3SystemServer(BackendApiResource.getInstance());
        singletons.add(server);

        // Add exception mappers:
        singletons.add(new V3InvalidValueExceptionMapper());
        singletons.add(new V3IOExceptionMapper());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
