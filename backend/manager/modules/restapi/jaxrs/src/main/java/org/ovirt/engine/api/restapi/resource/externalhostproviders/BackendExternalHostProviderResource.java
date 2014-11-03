/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendExternalHostProvidersResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourcesResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProviderResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;

public class BackendExternalHostProviderResource
        extends AbstractBackendExternalProviderResource<ExternalHostProvider>
        implements ExternalHostProviderResource {
    public BackendExternalHostProviderResource(String id) {
        super(id, ExternalHostProvider.class, SUB_COLLECTIONS);
    }

    @Override
    public ExternalComputeResourcesResource getExternalComputeResources() {
        return inject(new BackendExternalComputeResourcesResource(id));
    }

    @Override
    public ExternalDiscoveredHostsResource getExternalDiscoveredHosts() {
        return inject(new BackendExternalDiscoveredHostsResource(id));
    }

    @Override
    public ExternalHostGroupsResource getExternalHostGroups() {
        return inject(new BackendExternalHostGroupsResource(id));
    }

    @Override
    public ExternalHostsResource getExternalHosts() {
        return inject(new BackendExternalHostsResource(id));
    }
}
