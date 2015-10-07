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

import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.model.ExternalHosts;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalHostsResource
        extends AbstractBackendCollectionResource<ExternalHost, VDS>
        implements ExternalHostsResource {
    private String providerId;

    public BackendExternalHostsResource(String providerId) {
        super(ExternalHost.class, VDS.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalHosts list() {
        GetHostListFromExternalProviderParameters parameters = new GetHostListFromExternalProviderParameters();
        parameters.setFilterOutExistingHosts(true);
        parameters.setProviderId(asGuid(providerId));
        return mapCollection(getBackendCollection(VdcQueryType.GetHostListFromExternalProvider, parameters));
    }

    protected ExternalHosts mapCollection(List<VDS> entities) {
        ExternalHosts collection = new ExternalHosts();
        for (VDS entity : entities) {
            collection.getExternalHosts().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected ExternalHost addParents(ExternalHost entity) {
        ExternalHostProvider provider = new ExternalHostProvider();
        provider.setId(providerId);
        entity.setExternalHostProvider(provider);
        return super.addParents(entity);
    }

    @Override
    public ExternalHostResource getHostResource(String id) {
        return inject(new BackendExternalHostResource(id, providerId));
    }
}
