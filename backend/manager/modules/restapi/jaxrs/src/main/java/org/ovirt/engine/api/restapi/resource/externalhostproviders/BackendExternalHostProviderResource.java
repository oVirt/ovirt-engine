/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourcesResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProviderResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendExternalHostProviderResource
        extends AbstractBackendExternalProviderResource<ExternalHostProvider>
        implements ExternalHostProviderResource {

    public BackendExternalHostProviderResource(String id) {
        super(id, ExternalHostProvider.class);
    }

    @Override
    public ExternalHostProvider get() {
        return performGet(QueryType.GetProviderById, new IdQueryParameters(guid));
    }

    @Override
    public ExternalHostProvider update(ExternalHostProvider incoming) {
        return performUpdate(
            incoming,
            new QueryIdResolver<>(QueryType.GetProviderById, IdQueryParameters.class),
            ActionType.UpdateProvider,
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
        return performAction(ActionType.RemoveProvider, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<ExternalHostProvider, Provider> {
        @Override
        public ActionParametersBase getParameters(ExternalHostProvider incoming, Provider entity) {
            return new ProviderParameters(map(incoming, entity));
        }
    }
}
