/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeyResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;

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
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public OpenstackVolumeAuthenticationKey update(OpenstackVolumeAuthenticationKey resource) {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public Response remove() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }
}
