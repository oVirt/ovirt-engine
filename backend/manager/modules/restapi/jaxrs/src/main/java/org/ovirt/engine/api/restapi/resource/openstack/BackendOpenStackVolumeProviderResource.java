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

package org.ovirt.engine.api.restapi.resource.openstack;

import static org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackVolumeProvidersResource.SUB_COLLECTIONS;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeysResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendExternalProviderResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackVolumeProviderResource
        extends AbstractBackendExternalProviderResource<OpenStackVolumeProvider>
        implements OpenstackVolumeProviderResource {

    private BackendOpenStackVolumeProvidersResource parent;

    public BackendOpenStackVolumeProviderResource(String id, BackendOpenStackVolumeProvidersResource parent) {
        super(id, OpenStackVolumeProvider.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public OpenStackVolumeProvider get() {
        return performGet(VdcQueryType.GetProviderById, new IdQueryParameters(guid));
    }

    @Override
    public OpenStackVolumeProvider update(OpenStackVolumeProvider incoming) {
        return performUpdate(
            incoming,
            new QueryIdResolver<>(VdcQueryType.GetProviderById, IdQueryParameters.class),
            VdcActionType.UpdateProvider,
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
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(VdcActionType.RemoveProvider, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<OpenStackVolumeProvider, Provider> {
        @Override
        public VdcActionParametersBase getParameters(OpenStackVolumeProvider incoming, Provider entity) {
            return new ProviderParameters(map(incoming, entity));
        }
    }
}
