/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypeResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;

public class BackendOpenStackVolumeTypeResource
        extends AbstractBackendActionableResource<OpenStackVolumeType, CinderVolumeType>
        implements OpenstackVolumeTypeResource {
    private String providerId;

    protected BackendOpenStackVolumeTypeResource(String providerId, String id) {
        super(id, OpenStackVolumeType.class, CinderVolumeType.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackVolumeType get() {
        throw new UnsupportedOperationException("Cinder integration replaced by Managed Block Storage.\n"
                + "Please use Managed Block Storage for creating Cinderlib based storage domain.");
    }
}
