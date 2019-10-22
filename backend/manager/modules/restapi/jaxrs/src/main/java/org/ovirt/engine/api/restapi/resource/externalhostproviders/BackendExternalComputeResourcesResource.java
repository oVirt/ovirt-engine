/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import java.util.List;

import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.model.ExternalComputeResources;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourceResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourcesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendExternalComputeResourcesResource
        extends AbstractBackendCollectionResource<ExternalComputeResource, org.ovirt.engine.core.common.businessentities.ExternalComputeResource>
        implements ExternalComputeResourcesResource {
    private String providerId;

    public BackendExternalComputeResourcesResource(String providerId) {
        super(ExternalComputeResource.class, org.ovirt.engine.core.common.businessentities.ExternalComputeResource.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalComputeResources list() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        return mapCollection(getBackendCollection(QueryType.GetComputeResourceFromExternalProvider, parameters));
    }

    protected ExternalComputeResources mapCollection(
            List<org.ovirt.engine.core.common.businessentities.ExternalComputeResource> entities) {
        ExternalComputeResources collection = new ExternalComputeResources();
        for (org.ovirt.engine.core.common.businessentities.ExternalComputeResource entity : entities) {
            collection.getExternalComputeResources().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected ExternalComputeResource addParents(ExternalComputeResource model) {
        ExternalHostProvider provider = new ExternalHostProvider();
        provider.setId(providerId);
        model.setExternalHostProvider(provider);
        return super.addParents(model);
    }

    @Override
    public ExternalComputeResourceResource getResourceResource(String id) {
        return inject(new BackendExternalComputeResourceResource(id, providerId));
    }
}
