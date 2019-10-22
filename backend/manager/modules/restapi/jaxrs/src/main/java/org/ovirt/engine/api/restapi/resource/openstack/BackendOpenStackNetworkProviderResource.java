/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.resource.ExternalProviderCertificatesResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworksResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderCertificatesResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendOpenStackNetworkProviderResource
        extends AbstractBackendExternalProviderResource<OpenStackNetworkProvider>
        implements OpenstackNetworkProviderResource {

    public BackendOpenStackNetworkProviderResource(String id) {
        super(id, OpenStackNetworkProvider.class);
    }

    @Override
    public OpenStackNetworkProvider get() {
        return performGet(QueryType.GetProviderById, new IdQueryParameters(guid));
    }

    @Override
    public OpenStackNetworkProvider update(OpenStackNetworkProvider incoming) {
        return performUpdate(
            incoming,
            new QueryIdResolver<>(QueryType.GetProviderById, IdQueryParameters.class),
            ActionType.UpdateProvider,
            new UpdateParametersProvider()
        );
    }

    @Override
    public OpenstackNetworksResource getNetworksResource() {
        return inject(new BackendOpenStackNetworksResource(id));
    }

    @Override
    public ExternalProviderCertificatesResource getCertificatesResource() {
        return inject(new BackendExternalProviderCertificatesResource(id));
    }

    @Override
    public Response remove() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(ActionType.RemoveProvider, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<OpenStackNetworkProvider, Provider> {
        @Override
        public ActionParametersBase getParameters(OpenStackNetworkProvider incoming, Provider entity) {
            return new ProviderParameters(map(incoming, entity));
        }
    }
}
