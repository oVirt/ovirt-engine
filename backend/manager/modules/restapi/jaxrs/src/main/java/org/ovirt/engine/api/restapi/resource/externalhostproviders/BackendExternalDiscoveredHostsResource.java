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

import java.util.List;

import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.model.ExternalDiscoveredHosts;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalDiscoveredHostsResource
        extends AbstractBackendCollectionResource<ExternalDiscoveredHost, org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost>
        implements ExternalDiscoveredHostsResource {
    private String providerId;

    public BackendExternalDiscoveredHostsResource(String providerId) {
        super(ExternalDiscoveredHost.class, org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalDiscoveredHosts list() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        return mapCollection(getBackendCollection(VdcQueryType.GetDiscoveredHostListFromExternalProvider, parameters));
    }

    protected ExternalDiscoveredHosts mapCollection(
            List<org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost> entities) {
        ExternalDiscoveredHosts collection = new ExternalDiscoveredHosts();
        for (org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost entity : entities) {
            collection.getExternalDiscoveredHosts().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected ExternalDiscoveredHost addParents(ExternalDiscoveredHost image) {
        ExternalHostProvider provider = new ExternalHostProvider();
        provider.setId(providerId);
        image.setExternalHostProvider(provider);
        return super.addParents(image);
    }

    @Override
    public ExternalDiscoveredHostResource getHostResource(String id) {
        return inject(new BackendExternalDiscoveredHostResource(id, providerId));
    }
}
