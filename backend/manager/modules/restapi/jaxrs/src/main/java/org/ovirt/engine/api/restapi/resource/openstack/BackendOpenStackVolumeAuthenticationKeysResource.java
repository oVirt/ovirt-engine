/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKeys;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeyResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeysResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;

public class BackendOpenStackVolumeAuthenticationKeysResource
        extends AbstractBackendCollectionResource<OpenstackVolumeAuthenticationKey, LibvirtSecret>
        implements OpenstackVolumeAuthenticationKeysResource {
    private String providerId;

    public BackendOpenStackVolumeAuthenticationKeysResource(String providerId) {
        super(OpenstackVolumeAuthenticationKey.class, LibvirtSecret.class);
        this.providerId = providerId;
    }

    @Override
    public OpenstackVolumeAuthenticationKeys list() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public Response add(OpenstackVolumeAuthenticationKey authenticationKey) {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }

    @Override
    public OpenstackVolumeAuthenticationKeyResource getKeyResource(String id) {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }
}
