/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenStackVolumeProviders;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProvidersResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.Provider;

public class BackendOpenStackVolumeProvidersResource
        extends AbstractBackendCollectionResource<OpenStackVolumeProvider, Provider>
        implements OpenstackVolumeProvidersResource {

    public BackendOpenStackVolumeProvidersResource() {
        super(OpenStackVolumeProvider.class, Provider.class);
    }

    @Override
    public OpenStackVolumeProviders list() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public Response add(OpenStackVolumeProvider provider) {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public OpenstackVolumeProviderResource getProviderResource(String id) {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }
}
