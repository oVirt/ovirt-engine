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

import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.restapi.resource.BackendBookmarksResource;
import org.ovirt.engine.api.restapi.resource.BackendCapabilitiesResource;
import org.ovirt.engine.api.restapi.resource.BackendClustersResource;
import org.ovirt.engine.api.restapi.resource.BackendDataCentersResource;
import org.ovirt.engine.api.restapi.resource.BackendCpuProfilesResource;
import org.ovirt.engine.api.restapi.resource.BackendDiskProfilesResource;
import org.ovirt.engine.api.restapi.resource.BackendDisksResource;
import org.ovirt.engine.api.restapi.resource.BackendEventsResource;
import org.ovirt.engine.api.restapi.resource.BackendHostsResource;
import org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResource;
import org.ovirt.engine.api.restapi.resource.BackendJobsResource;
import org.ovirt.engine.api.restapi.resource.BackendMacPoolsResource;
import org.ovirt.engine.api.restapi.resource.BackendNetworksResource;
import org.ovirt.engine.api.restapi.resource.BackendOperatingSystemsResource;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.api.restapi.resource.BackendRolesResource;
import org.ovirt.engine.api.restapi.resource.BackendSchedulingPoliciesResource;
import org.ovirt.engine.api.restapi.resource.BackendSchedulingPolicyUnitsResource;
import org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResource;
import org.ovirt.engine.api.restapi.resource.BackendStorageServerConnectionsResource;
import org.ovirt.engine.api.restapi.resource.BackendSystemPermissionsResource;
import org.ovirt.engine.api.restapi.resource.BackendTagsResource;
import org.ovirt.engine.api.restapi.resource.BackendTemplatesResource;
import org.ovirt.engine.api.restapi.resource.BackendVmPoolsResource;
import org.ovirt.engine.api.restapi.resource.BackendVmsResource;
import org.ovirt.engine.api.restapi.resource.BackendVnicProfilesResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendDomainsResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendGroupsResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendUsersResource;
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendExternalHostProvidersResource;
import org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackImageProvidersResource;
import org.ovirt.engine.api.restapi.resource.validation.IOExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.JsonExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.MalformedIdExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.MappingExceptionMapper;
import org.ovirt.engine.api.restapi.resource.validation.ValidatorLocator;
import org.ovirt.engine.api.restapi.security.auth.SessionProcessor;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.api.restapi.util.SessionHelper;
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
    private final SessionHelper sessionHelper;

    // The reference to the backend bean:
    private BackendLocal backend;

    // The set of singletons:
    private final Set<Object> singletons = new HashSet<Object>();

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
            log.error("Can't find reference to backend bean.", exception);
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
        addResource(new BackendMacPoolsResource());
        addResource(new BackendStorageDomainsResource());
        addResource(new BackendTemplatesResource());
        addResource(new BackendInstanceTypesResource());
        addResource(new BackendNetworksResource());
        addResource(new BackendVmPoolsResource());
        addResource(new BackendDisksResource());
        addResource(new BackendTagsResource());
        addResource(new BackendBookmarksResource());
        addResource(new BackendRolesResource());
        addResource(new BackendUsersResource());
        addResource(new BackendGroupsResource());
        addResource(new BackendDomainsResource());
        addResource(new BackendJobsResource());
        addResource(new BackendStorageServerConnectionsResource());
        addResource(new BackendVnicProfilesResource());
        addResource(new BackendSchedulingPoliciesResource());
        addResource(new BackendSchedulingPolicyUnitsResource());
        addResource(new BackendSystemPermissionsResource());
        addResource(new BackendDiskProfilesResource());
        addResource(new BackendCpuProfilesResource());
        addResource(new BackendOperatingSystemsResource());
        addResource(new BackendExternalHostProvidersResource());
        addResource(new BackendOpenStackImageProvidersResource());

        final SessionProcessor processor = new SessionProcessor();
        processor.setBackend(backend);
        processor.setCurrent(current);
        processor.setSessionHelper(sessionHelper);
        singletons.add(processor);

        // Intercepter that maps exceptions cause by illegal guid string to 400 status (BAD_REQUEST).
        singletons.add(new MalformedIdExceptionMapper());
        singletons.add(new JsonExceptionMapper());
        singletons.add(new MappingExceptionMapper());
        singletons.add(new IOExceptionMapper());
    }

    private void addResource(final BackendResource resource) {
        resource.setMessageBundle(messageBundle);
        resource.setBackend(backend);
        resource.setSessionHelper(sessionHelper);
        resource.setMappingLocator(mappingLocator);
        resource.setValidatorLocator(validatorLocator);
        singletons.add(resource);
    }

    @Override
    public Set<Object> getSingletons () {
        return singletons;
    }
}
