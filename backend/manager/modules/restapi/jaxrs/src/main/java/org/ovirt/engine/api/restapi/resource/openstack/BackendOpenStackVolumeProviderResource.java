/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeysResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendOpenStackVolumeProviderResource
        extends AbstractBackendExternalProviderResource<OpenStackVolumeProvider>
        implements OpenstackVolumeProviderResource {

    private BackendOpenStackVolumeProvidersResource parent;

    public BackendOpenStackVolumeProviderResource(String id, BackendOpenStackVolumeProvidersResource parent) {
        super(id, OpenStackVolumeProvider.class);
        this.parent = parent;
    }

    @Override
    public OpenStackVolumeProvider get() {
        return performGet(QueryType.GetProviderById, new IdQueryParameters(guid));
    }

    @Override
    public OpenStackVolumeProvider update(OpenStackVolumeProvider incoming) {
        return performUpdate(
            incoming,
            new QueryIdResolver<>(QueryType.GetProviderById, IdQueryParameters.class),
            ActionType.UpdateProvider,
            new UpdateParametersProvider()
        );
    }

    @Override
    public OpenstackVolumeTypesResource getVolumeTypesResource() {
        return inject(new BackendOpenStackVolumeTypesResource(id));
    }

    @Override
    public OpenstackVolumeAuthenticationKeysResource getAuthenticationKeysResource() {
        return inject(new BackendOpenStackVolumeAuthenticationKeysResource(id));
    }

    @Override
    protected OpenStackVolumeProvider doPopulate(OpenStackVolumeProvider model, Provider entity) {
        return parent.doPopulate(model, entity);
    }

    BackendOpenStackVolumeProvidersResource getParent() {
        return parent;
    }

    @Override
    public Response remove() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider, isForce());
        return performAction(ActionType.RemoveProvider, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<OpenStackVolumeProvider, Provider> {
        @Override
        public ActionParametersBase getParameters(OpenStackVolumeProvider incoming, Provider entity) {
            return new ProviderParameters(map(incoming, entity));
        }
    }
}
