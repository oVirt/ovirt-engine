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
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public OpenStackVolumeProvider update(OpenStackVolumeProvider incoming) {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public OpenstackVolumeTypesResource getVolumeTypesResource() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public OpenstackVolumeAuthenticationKeysResource getAuthenticationKeysResource() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public Response remove() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }
}
