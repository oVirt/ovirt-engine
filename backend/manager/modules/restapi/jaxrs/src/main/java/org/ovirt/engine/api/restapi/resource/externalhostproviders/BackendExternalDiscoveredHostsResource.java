/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
import org.ovirt.engine.core.common.queries.QueryType;

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
        return mapCollection(getBackendCollection(QueryType.GetDiscoveredHostListFromExternalProvider, parameters));
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
