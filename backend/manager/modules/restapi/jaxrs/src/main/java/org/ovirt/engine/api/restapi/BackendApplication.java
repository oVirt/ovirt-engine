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

package org.ovirt.engine.api.restapi;

import java.util.HashSet;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.api.restapi.resource.validation.IOExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.JsonExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.MalformedIdExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.MappingExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.ValidatorLocator;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class BackendApplication extends Application {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    // The messages bundle:
    private final MessageBundle messageBundle;
    private final MappingLocator mappingLocator;
    private final ValidatorLocator validatorLocator;

    // The reference to the backend bean:
    private BackendLocal backend;

    // The set of singletons:
    private final Set<Object> singletons = new HashSet<>();

    public BackendApplication() throws Exception {
        // Create and load the message bundle:
        messageBundle = new MessageBundle();
        messageBundle.setPath(Messages.class.getName());
        messageBundle.populate();

        // Create and populate the mapping locator:
        mappingLocator = new MappingLocator();
        mappingLocator.populate();

        // Create and populate the validator locator:
        validatorLocator = new ValidatorLocator();
        validatorLocator.populate();

        // Lookup the backend bean:
        try {
            Context initial = new InitialContext();
            backend = (BackendLocal) initial.lookup("java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal");
        }
        catch (Exception exception) {
            log.error("Can't find reference to backend bean.", exception);
            throw exception;
        }

        // We create the resources here in order to avoid duplicates, as the
        // method to retrieve them can be called more than once by the
        // framework:
        addResource(new BackendApiResource());

        // Intercepter that maps exceptions cause by illegal guid string to 400 status (BAD_REQUEST).
        singletons.add(new MalformedIdExceptionMapper());
        singletons.add(new JsonExceptionMapper());
        singletons.add(new MappingExceptionMapper());
        singletons.add(new IOExceptionMapper());
    }

    private void addResource(final BackendResource resource) {
        resource.setMessageBundle(messageBundle);
        resource.setBackend(backend);
        resource.setMappingLocator(mappingLocator);
        resource.setValidatorLocator(validatorLocator);
        singletons.add(resource);
    }

    @Override
    public Set<Object> getSingletons () {
        return singletons;
    }
}
