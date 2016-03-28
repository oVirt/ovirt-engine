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

package org.ovirt.engine.api.restapi;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.restapi.resource.validation.IOExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.JsonExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.MalformedIdExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.MappingExceptionMapper;

@ApplicationPath("/v4")
public class BackendApplication extends Application {
    // The set of singletons:
    private final Set<Object> singletons = new HashSet<>();

    public BackendApplication() throws Exception {
        // Add the root resource:
        singletons.add(BackendApiResource.getInstance());

        // Add the exception mappers:
        singletons.add(new MalformedIdExceptionMapper());
        singletons.add(new JsonExceptionMapper());
        singletons.add(new MappingExceptionMapper());
        singletons.add(new IOExceptionMapper());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
