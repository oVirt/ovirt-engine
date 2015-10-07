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

import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.model.ExternalComputeResources;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourceResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourcesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
        return mapCollection(getBackendCollection(VdcQueryType.GetComputeResourceFromExternalProvider, parameters));
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
