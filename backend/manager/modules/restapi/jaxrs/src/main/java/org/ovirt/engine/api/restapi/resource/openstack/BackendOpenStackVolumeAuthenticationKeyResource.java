/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeyResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

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
        return performGet(QueryType.GetLibvirtSecretById, new IdQueryParameters(guid));
    }

    @Override
    public OpenstackVolumeAuthenticationKey update(OpenstackVolumeAuthenticationKey resource) {
        return performUpdate(
                resource,
                new QueryIdResolver<>(QueryType.GetLibvirtSecretById, IdQueryParameters.class),
                ActionType.UpdateLibvirtSecret,
                new UpdateParametersProvider());
    }

    private class UpdateParametersProvider implements ParametersProvider<OpenstackVolumeAuthenticationKey, org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret> {
        @Override
        public ActionParametersBase getParameters(OpenstackVolumeAuthenticationKey model,
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
        return performAction(ActionType.RemoveLibvirtSecret, parameters);
    }

    @Override
    protected OpenstackVolumeAuthenticationKey addParents(OpenstackVolumeAuthenticationKey authenticationKey) {
        OpenStackVolumeProvider provider = new OpenStackVolumeProvider();
        provider.setId(providerId);
        authenticationKey.setOpenstackVolumeProvider(provider);
        return super.addParents(authenticationKey);
    }
}
