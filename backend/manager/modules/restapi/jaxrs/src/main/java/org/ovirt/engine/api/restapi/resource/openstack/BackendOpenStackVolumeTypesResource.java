/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.model.OpenStackVolumeTypes;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypeResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;

public class BackendOpenStackVolumeTypesResource
        extends AbstractBackendCollectionResource<OpenStackVolumeType, CinderVolumeType>
        implements OpenstackVolumeTypesResource {
    private String providerId;

    public BackendOpenStackVolumeTypesResource(String providerId) {
        super(OpenStackVolumeType.class, CinderVolumeType.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackVolumeTypes list() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");

    }

    @Override
    public OpenstackVolumeTypeResource getTypeResource(String id) {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }
}
