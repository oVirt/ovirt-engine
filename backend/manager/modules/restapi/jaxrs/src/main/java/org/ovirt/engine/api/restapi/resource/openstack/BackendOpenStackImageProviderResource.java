/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.resource.openstack.OpenstackImageProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImagesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendOpenStackImageProviderResource
        extends AbstractBackendExternalProviderResource<OpenStackImageProvider>
        implements OpenstackImageProviderResource {

    public BackendOpenStackImageProviderResource(String id) {
        super(id, OpenStackImageProvider.class);
    }

    @Override
    public OpenStackImageProvider get() {
        return performGet(QueryType.GetProviderById, new IdQueryParameters(guid));
    }

    @Override
    public OpenStackImageProvider update(OpenStackImageProvider incoming) {
        return performUpdate(
            incoming,
            new QueryIdResolver<>(QueryType.GetProviderById, IdQueryParameters.class),
            ActionType.UpdateProvider,
            new UpdateParametersProvider()
        );
    }

    @Override
    public OpenstackImagesResource getImagesResource() {
        return inject(new BackendOpenStackImagesResource(id));
    }

    @Override
    public Response remove() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(ActionType.RemoveProvider, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<OpenStackImageProvider, Provider> {
        @Override
        public ActionParametersBase getParameters(OpenStackImageProvider incoming, Provider entity) {
            return new ProviderParameters(map(incoming, entity));
        }
    }
}
