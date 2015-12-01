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

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourcesResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProviderResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalHostProviderResource
        extends AbstractBackendExternalProviderResource<ExternalHostProvider>
        implements ExternalHostProviderResource {

    public BackendExternalHostProviderResource(String id) {
        super(id, ExternalHostProvider.class, SUB_COLLECTIONS);
    }

    @Override
    public ExternalHostProvider get() {
        return performGet(VdcQueryType.GetProviderById, new IdQueryParameters(guid));
    }

    @Override
    public ExternalHostProvider update(ExternalHostProvider incoming) {
        return performUpdate(
            incoming,
            new QueryIdResolver<>(VdcQueryType.GetProviderById, IdQueryParameters.class),
            VdcActionType.UpdateProvider,
            new UpdateParametersProvider()
        );
    }

    @Override
    public ExternalComputeResourcesResource getComputeResourcesResource() {
        return inject(new BackendExternalComputeResourcesResource(id));
    }

    @Override
    public ExternalDiscoveredHostsResource getDiscoveredHostsResource() {
        return inject(new BackendExternalDiscoveredHostsResource(id));
    }

    @Override
    public ExternalHostGroupsResource getHostGroupsResource() {
        return inject(new BackendExternalHostGroupsResource(id));
    }

    @Override
    public ExternalHostsResource getHostsResource() {
        return inject(new BackendExternalHostsResource(id));
    }


    @Override
    public Response remove() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(VdcActionType.RemoveProvider, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<ExternalHostProvider, Provider> {
        @Override
        public VdcActionParametersBase getParameters(ExternalHostProvider incoming, Provider entity) {
            return new ProviderParameters(map(incoming, entity));
        }
    }
}
