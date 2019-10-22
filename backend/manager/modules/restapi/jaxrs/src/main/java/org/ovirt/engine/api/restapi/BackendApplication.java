/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
import org.ovirt.engine.api.restapi.resource.validation.ValidationExceptionMapper;

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
        singletons.add(new ValidationExceptionMapper());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
