/*
 * Copyright (c) 2015 Red Hat, Inc.
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

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeyResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendOpenStackVolumeAuthenticationKeyResource
        extends AbstractBackendActionableResource<OpenstackVolumeAuthenticationKey, LibvirtSecret>
        implements OpenstackVolumeAuthenticationKeyResource {
    private String providerId;

    protected BackendOpenStackVolumeAuthenticationKeyResource(String providerId, String id) {
        super(id, OpenstackVolumeAuthenticationKey.class, LibvirtSecret.class);
        this.providerId = providerId;
    }

    @Override
    public OpenstackVolumeAuthenticationKey get() {
        return performGet(VdcQueryType.GetLibvirtSecretById, new IdQueryParameters(guid));
    }

    @Override
    public OpenstackVolumeAuthenticationKey update(OpenstackVolumeAuthenticationKey resource) {
        return performUpdate(
                resource,
                new QueryIdResolver<>(VdcQueryType.GetLibvirtSecretById, IdQueryParameters.class),
                VdcActionType.UpdateLibvirtSecret,
                new UpdateParametersProvider());
    }

    private class UpdateParametersProvider implements ParametersProvider<OpenstackVolumeAuthenticationKey, org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret> {
        @Override
        public VdcActionParametersBase getParameters(OpenstackVolumeAuthenticationKey model,
                org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret entity) {
            final org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret libvirtSecret =
                    map(model, entity);
            return new LibvirtSecretParameters(libvirtSecret);
        }
    }

    @Override
    public Response remove() {
        LibvirtSecret libvirtSecret = map(get(), null);
        LibvirtSecretParameters parameters = new LibvirtSecretParameters(libvirtSecret);
        return performAction(VdcActionType.RemoveLibvirtSecret, parameters);
    }

    @Override
    protected OpenstackVolumeAuthenticationKey addParents(OpenstackVolumeAuthenticationKey authenticationKey) {
        OpenStackVolumeProvider provider = new OpenStackVolumeProvider();
        provider.setId(providerId);
        authenticationKey.setOpenstackVolumeProvider(provider);
        return super.addParents(authenticationKey);
    }
}
