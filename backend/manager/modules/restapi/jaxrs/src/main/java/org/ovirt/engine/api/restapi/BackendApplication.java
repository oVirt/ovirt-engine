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

import org.apache.log4j.Logger;
import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.security.auth.BasicAuthorizationScheme;
import org.ovirt.engine.api.common.security.auth.Challenger;
import org.ovirt.engine.api.resource.CapabilitiesResource;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.logging.RequestPayloadLogger;
import org.ovirt.engine.api.restapi.logging.RequestVerbLogger;
import org.ovirt.engine.api.restapi.logging.ResponsePayloadLogger;
import org.ovirt.engine.api.restapi.logging.ResponseStatusLogger;
import org.ovirt.engine.api.restapi.resource.AbstractBackendResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.restapi.resource.BackendCapabilitiesResource;
import org.ovirt.engine.api.restapi.resource.BackendClustersResource;
import org.ovirt.engine.api.restapi.resource.BackendDataCentersResource;
import org.ovirt.engine.api.restapi.resource.BackendDisksResource;
import org.ovirt.engine.api.restapi.resource.BackendDomainsResource;
import org.ovirt.engine.api.restapi.resource.BackendEventsResource;
import org.ovirt.engine.api.restapi.resource.BackendGroupsResource;
import org.ovirt.engine.api.restapi.resource.BackendHostsResource;
import org.ovirt.engine.api.restapi.resource.BackendJobsResource;
import org.ovirt.engine.api.restapi.resource.BackendNetworksResource;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.api.restapi.resource.BackendRolesResource;
import org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResource;
import org.ovirt.engine.api.restapi.resource.BackendTagsResource;
import org.ovirt.engine.api.restapi.resource.BackendTemplatesResource;
import org.ovirt.engine.api.restapi.resource.BackendUsersResource;
import org.ovirt.engine.api.restapi.resource.BackendVmPoolsResource;
import org.ovirt.engine.api.restapi.resource.BackendVmsResource;
import org.ovirt.engine.api.restapi.resource.validation.MalformedIdExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.ValidatorLocator;
import org.ovirt.engine.api.restapi.security.auth.LoginValidator;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.api.restapi.util.SessionHelper;
import org.ovirt.engine.core.common.interfaces.BackendLocal;

@ApplicationPath("/")
public class BackendApplication extends Application {
    // The log:
    private static final Logger logger = Logger.getLogger(BackendApplication.class);

    // The messages bundle:
    private MessageBundle messageBundle;
    private MappingLocator mappingLocator;
    private ValidatorLocator validatorLocator;
    private SessionHelper sessionHelper;

    // The reference to the backend bean:
    private BackendLocal backend;

    // The set of singletons:
    private Set<Object> singletons = new HashSet<Object>();

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

        // Create the session helper:
        final Current current = new Current();
        singletons.add(current);
        sessionHelper = new SessionHelper();
        sessionHelper.setCurrent(current);

        // Lookup the backend bean:
        try {
            Context initial = new InitialContext();
            backend = (BackendLocal) initial.lookup("java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal");
        }
        catch (Exception exception) {
            logger.error("Can't find reference to backend bean.", exception);
            throw exception;
        }

        // We create the resources here in order to avoid duplicates, as the
        // method to retrieve them can be called more than once by the
        // framework:
        addResource(new BackendApiResource());
        addResource(new BackendEventsResource());
        addResource(new BackendCapabilitiesResource());
        addResource(new BackendVmsResource());
        addResource(new BackendHostsResource());
        addResource(new BackendClustersResource());
        addResource(new BackendDataCentersResource());
        addResource(new BackendStorageDomainsResource());
        addResource(new BackendTemplatesResource());
        addResource(new BackendNetworksResource());
        addResource(new BackendVmPoolsResource());
        addResource(new BackendDisksResource());
        addResource(new BackendTagsResource());
        addResource(new BackendRolesResource());
        addResource(new BackendUsersResource());
        addResource(new BackendGroupsResource());
        addResource(new BackendDomainsResource());
        addResource(new BackendJobsResource());

        // Authentication singletons:
        final BasicAuthorizationScheme scheme = new BasicAuthorizationScheme();
        final LoginValidator validator = new LoginValidator();
        validator.setBackend(backend);
        validator.setCurrent(current);
        validator.setSessionHelper(sessionHelper);
        singletons.add(validator);

        final Challenger challenger = new Challenger();
        challenger.setRealm("ENGINE");
        challenger.setScheme(scheme);
        challenger.setValidator(validator);
        challenger.setCurrent(current);
        singletons.add(challenger);

        // Logging infrastructure:
        singletons.add(new RequestVerbLogger());
        singletons.add(new RequestPayloadLogger());
        singletons.add(new ResponseStatusLogger());
        singletons.add(new ResponsePayloadLogger());

        // Intercepter that maps exceptions cause by illegal guid string to 400 status (BAD_REQUEST).
        singletons.add(new MalformedIdExceptionMapper());
    }

    private void addResource(final BackendResource resource) {
        resource.setMessageBundle(messageBundle);
        resource.setBackend(backend);
        resource.setSessionHelper(sessionHelper);
        if (resource instanceof AbstractBackendResource) {
            ((AbstractBackendResource) resource).setMappingLocator(mappingLocator);
        }
        else if (resource instanceof CapabilitiesResource) {
            ((BackendCapabilitiesResource) resource).setMappingLocator(mappingLocator);
        }
        resource.setValidatorLocator(validatorLocator);
        singletons.add(resource);
    }

    @Override
    public Set<Object> getSingletons () {
        return singletons;
    }
}
